package com.ljmarinscull.baubuddy.domain.repository

import com.ljmarinscull.baubuddy.data.datasource.local.ILocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.IRemoteDataSource
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.ui.home.CompoundQuery
import kotlinx.coroutines.flow.Flow

class Repository(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource
) : IRepository {
    override suspend fun login(username: String, password: String): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun requestResources(authorization: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun loadResources(query: CompoundQuery): Flow<List<Resource>> {
        TODO("Not yet implemented")
    }

}