package com.ljmarinscull.baubuddy.domain.repository

import com.ljmarinscull.baubuddy.data.datasource.local.ILocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.IRemoteDataSource
import com.ljmarinscull.baubuddy.data.models.toResource
import com.ljmarinscull.baubuddy.data.models.toResourceEntity
import com.ljmarinscull.baubuddy.ui.home.FilterType
import kotlinx.coroutines.flow.mapNotNull

class Repository(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource
) : IRepository {
    override suspend fun login(username: String, password: String): Result<String> {
        val authorizationResult = remoteDataSource.login(username, password)
        return authorizationResult.fold(
            onSuccess = {
                Result.success(authorizationResult.getOrNull()?.authorization ?: "")
            }, onFailure = {
                Result.failure(it)
            }
        )
    }

    override fun loadResources(filterType: FilterType) = when(filterType){
        is FilterType.CompoundQuery -> {
            localDataSource
                .getAll(filterType.query, filterType.isAvailable)
                .mapNotNull { list ->
                    list.map { entity ->
                        entity.toResource()
                    }
                }
        }
        is FilterType.Availability -> {
            localDataSource
                .getAll(filterType.isAvailable)
                .mapNotNull { list ->
                    list.map { entity ->
                        entity.toResource()
                    }
                }
        } is FilterType.Query ->{
            localDataSource
                .getAll(filterType.query)
                .mapNotNull { list ->
                    list.map { entity ->
                        entity.toResource()
                    }
                }
        }
    }

    override suspend fun requestResources(authorization: String): Result<Unit> {
        val result = remoteDataSource.requestResources(authorization)
        return if (result.isSuccess) {
            localDataSource.save(
                result.getOrDefault(emptyList())
                    .map { it.toResourceEntity() }
            )
            Result.success(Unit)
        } else {
            Result.failure(
                result.exceptionOrNull()!!
            )
        }
    }

}