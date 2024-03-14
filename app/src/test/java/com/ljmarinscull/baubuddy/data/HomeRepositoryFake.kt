
package com.ljmarinscull.baubuddy.data

import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSourceError
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.domain.repository.IHomeRepository
import com.ljmarinscull.baubuddy.ui.home.CompoundQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HomeRepositoryFake: IHomeRepository {

    var errorToReturn: RemoteDataSourceError? = null

    override suspend fun requestResources(authorization: String): Result<Unit> =
         if(errorToReturn != null) {
            Result.failure(errorToReturn!!)
        } else Result.success(Unit)


    override fun loadResources(query: CompoundQuery): Flow<List<Resource>> = flow {
        listOf(
            Resource(
            task = "Task 1",
            title = "Title 1",
            description = "Description 1",
            sort = "1",
            wageType = "WageType 1",
            businessUnitKey = "BusinessUnitKey 1",
            businessUnit = "BusinessUnit 1",
            parentTaskID = "ParentTaskID 1",
            prePlanningBoardQuickSelect = "PrePlanningBoardQuickSelect 1",
            colorCode = "#efef00",
            workingTime = "workingTime 1",
            isAvailableInTimeTrackingKioskMode = true,
        )
        )
    }
}

