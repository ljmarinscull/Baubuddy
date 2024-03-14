package com.ljmarinscull.baubuddy.data.datasource.remote

import com.ljmarinscull.baubuddy.data.models.RemoteResourceJO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import java.io.IOException

interface IRemoteDataSource {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun requestResources(authorization: String): Result<List<RemoteResourceJO>>
}

data class RemoteDataSourceError(
    override val message: String,
    val errorType: ErrorType,
) : Exception(message)

enum class ErrorType {
    CONNEXION,
    INVALID_TOKEN,
    OTHER
}

class RemoteDataSource(
    private val api: ApiService
) : IRemoteDataSource {

    override suspend fun login(username: String, password: String): Result<LoginResponse> =
        coroutineScope {
            val loginResult = async {
                try {
                    Result.success(
                        api.login(
                            UserCredentials(username, password)
                        )
                    )
                } catch (e: HttpException) {
                    Result.failure(RemoteDataSourceError("Unexpected error.", ErrorType.OTHER))
                } catch (e: IOException) {
                    Result.failure(
                        RemoteDataSourceError(
                            "Check your internet connexion.",
                            ErrorType.CONNEXION
                        )
                    )
                }
            }

            val data = loginResult.await().getOrNull()
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(
                    loginResult.await().exceptionOrNull() ?: Exception("UNKNOWN_ERROR")
                )
            }
        }

    override suspend fun requestResources(authorization: String): Result<List<RemoteResourceJO>> =
        coroutineScope {
            val reqResourcesResult = async {
                try {
                    Result.success(
                        api.requestResources(
                            authorization
                        )
                    )
                } catch (e: HttpException) {
                    Result.failure(
                        RemoteDataSourceError(
                            "Token not valid.",
                            ErrorType.INVALID_TOKEN
                        )
                    )
                } catch (e: IOException) {
                    Result.failure(
                        RemoteDataSourceError(
                            "Check your internet connexion.",
                            ErrorType.CONNEXION
                        )
                    )
                }
            }

            val data = reqResourcesResult.await().getOrNull()
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(
                    reqResourcesResult.await().exceptionOrNull() ?: Exception("UNKNOWN_ERROR")
                )
            }
        }
}