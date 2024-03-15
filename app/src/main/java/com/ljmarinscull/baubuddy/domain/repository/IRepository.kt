package com.ljmarinscull.baubuddy.domain.repository

import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.ui.home.FilterType
import kotlinx.coroutines.flow.Flow

fun interface ILoginRepository{
    suspend fun login(username: String, password: String): Result<String>
}
interface IHomeRepository{
    suspend fun requestResources(authorization: String): Result<Unit>
    fun loadResources(filterType: FilterType): Flow<List<Resource>>
}
interface IRepository: ILoginRepository,IHomeRepository
