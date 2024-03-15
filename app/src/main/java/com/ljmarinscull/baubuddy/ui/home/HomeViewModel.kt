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
import com.ljmarinscull.baubuddy.ui.home.FilterType.Companion.SEPARATOR
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
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

    init {
        state.combine(resources){ state, resources ->
            if (state.resources != resources){
                _state.update {
                    it.copy(
                        resources = resources
                    )
                }
            } else state
        }.launchIn(viewModelScope)
    }
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

    private fun handleQuery(query: String?): FilterType {
        if (query.isNullOrEmpty())
            return FilterType.Query()

        val filterType = FilterType.filterAvailabilityFrom(query)
        if (filterType != null)
            return filterType

        if (query.contains(SEPARATOR)) {
            val splitQuery = query.split(SEPARATOR, limit = 2)
            if (splitQuery.size == 2) {
                val type = FilterType.filterAvailabilityFrom(splitQuery[1])
                if(type != null)
                    return FilterType.CompoundQuery(splitQuery[0], type.isAvailable)
                else
                    FilterType.Query(splitQuery[0])
            }
        }

        return FilterType.Query(query)
    }

    fun onRefresh() {
        refreshing()
        requestItems()
    }
    fun filterByQuery(text: String) {
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            delay(1000L)
            _state.update { it.copy(
                query = text.ifEmpty { null }
            )}
        }
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
    }
    fun updateValidAuthorization(value: Boolean) {
        _state.update {
            it.copy(
                validAuthorization = value
            )
        }
    }
}

class HomeViewModelFactory(
    private val repository: IRepository,
    private val preferencesRepository: IPreferencesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
