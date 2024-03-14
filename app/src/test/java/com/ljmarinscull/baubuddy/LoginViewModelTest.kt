package com.ljmarinscull.baubuddy


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.ljmarinscull.baubuddy.data.LoginRepositoryFake
import com.ljmarinscull.baubuddy.data.PreferencesRepositoryFake
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.ui.login.LoginEvent
import com.ljmarinscull.baubuddy.ui.login.LoginViewModel
import com.ljmarinscull.baubuddy.util.MainCoroutineExtension
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.rules.TestRule
data class RemoteDataSourceError(
    override val message: String,
    val errorType: ErrorType,
) : Exception(message)

enum class ErrorType {
    CONNEXION,
    INVALID_TOKEN,
    OTHER
}
@ExtendWith(MainCoroutineExtension::class)
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private lateinit var repository: LoginRepositoryFake
    private lateinit var appPreferences: IPreferencesRepository

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()
    private val dispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setUp() {
        repository = LoginRepositoryFake()
        appPreferences = PreferencesRepositoryFake()
        viewModel = LoginViewModel(
            repository = repository,
            preferencesRepository = appPreferences,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `Show and hide the loading indicator during Login process`() = runBlocking {
        viewModel.state.test {
            val emission1 = awaitItem()
            assertThat(emission1.isLoading).isFalse()

            viewModel.onEvent(LoginEvent.Login("username", "password"))

            val emission2 = awaitItem()
            assertThat(emission2.isLoading).isTrue()

            val emission3 = awaitItem()
            assertThat(emission3.isLoading).isFalse()
        }
    }

    @Test
    fun `Hide the loading indicator in case we get an error during Login process`() = runBlocking {
        repository.errorToReturn = RemoteDataSourceError("Invalid token", ErrorType.INVALID_TOKEN)

        viewModel.state.test {
            val emission1 = awaitItem()
            assertThat(emission1.isLoading).isFalse()

            viewModel.onEvent(LoginEvent.Login("username", "password"))

            val emission2 = awaitItem()
            assertThat(emission2.isLoading).isTrue()

            val emission3 = awaitItem()
            assertThat(emission3.isLoading).isFalse()
        }
    }

    @Test
    fun `Getting the authorization token on success Login`() = runBlocking {

        viewModel.userLoggedState.test {
            val emission1 = awaitItem()
            assertThat(emission1.authorization).isEqualTo("")

            viewModel.onEvent(LoginEvent.Login("username", "password"))

            val emission2 = awaitItem()
            assertThat(emission2.authorization).isEqualTo("authorization")
        }
    }

}