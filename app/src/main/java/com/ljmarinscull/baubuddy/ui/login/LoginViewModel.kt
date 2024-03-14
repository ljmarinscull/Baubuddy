package com.ljmarinscull.baubuddy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ljmarinscull.baubuddy.data.datasource.local.AppPreferences
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.domain.repository.ILoginRepository
import com.ljmarinscull.baubuddy.domain.repository.IRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

data class LoginState(
    val isLoading: Boolean = false,
)

sealed class LoginEvent {
    data class Login(val username: String, val password: String) : LoginEvent()
}

class LoginViewModel(
    private val repository: ILoginRepository,
    private val preferencesRepository: IPreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private  val errorChannel = Channel<String>()
    val errorFlow = errorChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    val userLoggedState: StateFlow<AppPreferences> = preferencesRepository.appPreferencesFlow
        .stateIn(
            viewModelScope + dispatcher,
            SharingStarted.WhileSubscribed(5000L),
            AppPreferences("")
        )

    private fun login(username: String, password: String) {
        viewModelScope.launch(dispatcher) {
            loading()
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { data ->
                    loading(false)
                    preferencesRepository.updateAuthorization(data)
                },
                onFailure = { throwable ->
                    loading(false)
                    errorChannel.send(throwable.localizedMessage!!)
                }
            )
        }
    }

    private fun loading(value: Boolean = true) {
        _state.update {
            it.copy(
                isLoading = value
            )
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> {
                login(event.username, event.password)
            }
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