package com.ljmarinscull.baubuddy.domain.repository

import com.ljmarinscull.baubuddy.data.datasource.remote.LoginResponse
import com.ljmarinscull.baubuddy.data.models.RemoteResourceJO
import com.ljmarinscull.baubuddy.data.models.ResourceEntity
import kotlinx.coroutines.flow.Flow

interface IRemoteDataSource {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun requestResources(authorization: String): Result<List<RemoteResourceJO>>
}

interface ILocalDataSource {
    suspend fun save(resources: List<ResourceEntity>)
    fun getAll(query: String, isAvailable: Boolean): Flow<List<ResourceEntity>>
    fun getAll(query: String): Flow<List<ResourceEntity>>
}

class Repository(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource
) : IRepository {
    override suspend fun login(username: String, password: String): Result<String> {
        TODO("Not yet implemented")
    }

}