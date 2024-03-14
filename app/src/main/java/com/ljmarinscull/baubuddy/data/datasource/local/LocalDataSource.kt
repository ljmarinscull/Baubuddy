package com.ljmarinscull.baubuddy.data.datasource.local

import com.ljmarinscull.baubuddy.ResourcesDao
import com.ljmarinscull.baubuddy.data.models.ResourceEntity
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {
    suspend fun save(resources: List<ResourceEntity>)
    fun getAll(query: String, isAvailable: Boolean): Flow<List<ResourceEntity>>
    fun getAll(query: String): Flow<List<ResourceEntity>>
}

class LocalDataSource(
    private val resourcesDao: ResourcesDao
) : ILocalDataSource {

    override suspend fun save(resources: List<ResourceEntity>) {
        resourcesDao.save(resources)
    }

    override fun getAll(query: String, isAvailable: Boolean): Flow<List<ResourceEntity>> = resourcesDao
            .findAll(query, isAvailable)

    override fun getAll(query: String) = resourcesDao.findAll(query)
}