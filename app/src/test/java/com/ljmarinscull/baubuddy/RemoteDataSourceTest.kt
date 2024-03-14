package com.ljmarinscull.baubuddy

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.ljmarinscull.baubuddy.data.ApiServiceFake
import com.ljmarinscull.baubuddy.data.datasource.remote.ErrorType
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSourceError
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import java.io.IOException

class RemoteDataSourceTest {
    private lateinit var remoteDataSource: RemoteDataSource

    private lateinit var api: ApiServiceFake

    @BeforeEach
    fun setUp() {
        api = ApiServiceFake()

        remoteDataSource = RemoteDataSource(
            api
        )
    }

    @Test
    fun `Repository returns an expected list of users`() = runBlocking {

        val usersResult = remoteDataSource.requestResources("")

        assertThat(usersResult.isSuccess).isTrue()
        assertThat(usersResult.getOrThrow().size).isEqualTo(api.resources.size)
    }

    @Test
    fun `Repository returns an expected list of users1`() = runBlocking {
         api.errorToReturn = IOException()
        val usersResult = remoteDataSource.requestResources("")

        assertThat(usersResult.isFailure).isTrue()
        val exception = usersResult.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat((exception as RemoteDataSourceError).errorType).isEqualTo(ErrorType.CONNEXION)
    }

    @Test
    fun `Repository returns an expected list of users2`() = runBlocking {

        api.errorToReturn = mockk<HttpException>()
        val usersResult = remoteDataSource.requestResources("")

        assertThat(usersResult.isFailure).isTrue()
        val exception = usersResult.exceptionOrNull()
        assertThat(exception).isNotNull()
        assertThat((exception as RemoteDataSourceError).errorType).isEqualTo(ErrorType.INVALID_TOKEN)
    }
}