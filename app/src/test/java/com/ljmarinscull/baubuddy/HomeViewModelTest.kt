package com.ljmarinscull.baubuddy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.ljmarinscull.baubuddy.data.HomeRepositoryFake
import com.ljmarinscull.baubuddy.data.PreferencesRepositoryFake
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.remote.ErrorType
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSourceError
import com.ljmarinscull.baubuddy.ui.home.HomeViewModel
import com.ljmarinscull.baubuddy.util.MainCoroutineExtension
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.rules.TestRule

@ExtendWith(MainCoroutineExtension::class)
class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: HomeRepositoryFake
    private lateinit var appPreferences: IPreferencesRepository

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()
    private val dispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setUp() {
        repository = HomeRepositoryFake()
        appPreferences = PreferencesRepositoryFake()
        viewModel = HomeViewModel(
            repository = repository,
            appPreferencesRepository = appPreferences,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `Show and hide the loading indicator during Request items process`() = runBlocking {
        viewModel.state.test {
            val emission1 = awaitItem()
            assertThat(emission1.isRefreshing).isFalse()

            viewModel.onRefresh()
            val emission2 = awaitItem()
            assertThat(emission2.isRefreshing).isTrue()

            val emission3 = awaitItem()
            assertThat(emission3.isRefreshing).isFalse()
        }
    }

    @Test
    fun `Hide the loading indicator in case we get an error during Login process`() = runBlocking {
        repository.errorToReturn = RemoteDataSourceError("Invalid token", ErrorType.INVALID_TOKEN)

        viewModel.state.test {
            val emission1 = awaitItem()
            assertThat(emission1.isRefreshing).isFalse()
            assertThat(emission1.validAuthorization).isFalse()

            viewModel.onRefresh()
            val emission2 = awaitItem()
            assertThat(emission2.isRefreshing).isTrue()
            assertThat(emission2.validAuthorization).isFalse()

            val emission3 = awaitItem()
            assertThat(emission3.isRefreshing).isFalse()
            assertThat(emission3.validAuthorization).isFalse()
        }
    }
}