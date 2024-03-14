package com.ljmarinscull.baubuddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.remote.ErrorType
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSourceError
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.domain.repository.IHomeRepository
import com.ljmarinscull.baubuddy.domain.repository.IRepository
import com.ljmarinscull.baubuddy.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val query: String? = null,
    val isRefreshing: Boolean = false,
    val validAuthorization: Boolean = false,
    val resources: List<Resource> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: IHomeRepository,
    private val appPreferencesRepository: IPreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    val resources = state.flatMapLatest { state ->
        repository.loadResources(handleQuery(state.query))
    }

    private val errorChannel = Channel<String>()
    val errorFlow = errorChannel.receiveAsFlow()

    private var requestItemsJob: Job? = null
    private var queryJob: Job? = null

    private fun requestItems() {
        requestItemsJob?.cancel()
        requestItemsJob = viewModelScope.launch(dispatcher) {
            val authorization = appPreferencesRepository.fetchPreferences().authorization
            val result = repository.requestResources(authorization)
            refreshing(false)

            result.fold(onSuccess = {},
                onFailure = { error ->
                    if (error is RemoteDataSourceError && error.errorType == ErrorType.INVALID_TOKEN) {
                        appPreferencesRepository.updateAuthorization("")
                        _state.update {
                            it.copy(
                                validAuthorization = false,
                            )
                        }
                    } else {
                        error.message?.let {
                            errorChannel.send(it)
                        }
                    }
                })
        }
    }

    private fun handleQuery(query: String?): CompoundQuery {
        if (query.isNullOrEmpty())
            return CompoundQuery()

        if (FilterType.filterFrom(query) == FilterType.AVAILABLE)
            return CompoundQuery().copy(
                filterType = FilterType.AVAILABLE
            )

        if (FilterType.filterFrom(query) == FilterType.NOT_AVAILABLE)
            return CompoundQuery().copy(
                filterType = FilterType.NOT_AVAILABLE
            )

        if (query.contains(CompoundQuery.SEPARATOR)) {
            val splitQuery = query.split(CompoundQuery.SEPARATOR, limit = 2)
            if (splitQuery.size == 2) {
                val type = FilterType.filterFrom(splitQuery[1])
                return CompoundQuery(
                    text = splitQuery[0],
                    filterType = type
                )
            }
        }

        return CompoundQuery(
            query,
            FilterType.SIMPLE
        )
    }

    fun onRefresh() {
        refreshing()
        requestItems()
    }

    fun setQuery(text: String) {
        _state.update {
            it.copy(
                query = text.ifEmpty { null }
            )
        }
    }

    private fun refreshing(isRefreshing: Boolean = true) {
        _state.update {
            it.copy(
                isRefreshing = isRefreshing
            )
        }
        FilterType.AVAILABLE.pattern
    }
}

class HomeViewModelFactory(
    private val repository: IRepository,
    private val preferencesRepository: IPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
