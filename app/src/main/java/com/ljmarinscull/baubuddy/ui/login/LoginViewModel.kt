package com.ljmarinscull.baubuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.domain.repository.ILoginRepository
import com.ljmarinscull.baubuddy.domain.repository.IRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val isLoading: Boolean = false,
    val loginButtonEnable: Boolean = true
)
class LoginViewModel(
    private val repository: ILoginRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {


    private  val errorChannel = Channel<String>()
    val errorFlow = errorChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    val userLogged = preferencesRepository.appPreferencesFlow.asLiveData()

    fun login(username: String, password: String) = liveData(dispatcher) {
        loading()
        val result = repository.login(username, password)
        loading(false)
        result.fold(
            onSuccess = { data ->
                preferencesRepository.updateAuthorization(data)
                emit(true)
            },
            onFailure = { throwable ->
                errorChannel.send(throwable.localizedMessage!!)
                emit(false)
            }
        )
    }

    private fun loading(value: Boolean = true) {
        _state.update {
            it.copy(
                isLoading = value
            )
        }
    }
}

class LoginViewModelFactory(
    private val repository: IRepository,
    private val preferencesRepository: IPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}