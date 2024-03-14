
package com.ljmarinscull.baubuddy

import com.ljmarinscull.baubuddy.data.datasource.remote.ApiService
import com.ljmarinscull.baubuddy.data.datasource.remote.LoginResponse
import com.ljmarinscull.baubuddy.data.datasource.remote.OAuthJO
import com.ljmarinscull.baubuddy.data.datasource.remote.UserCredentials
import com.ljmarinscull.baubuddy.data.models.RemoteResourceJO

class ApiServiceFake: ApiService {

    var errorToReturn: Exception? = null

    val resources = listOf(
        RemoteResourceJO(
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
        ),
        RemoteResourceJO(
            task = "Task 2",
            title = "Title 2",
            description = "Description 2",
            sort = "0",
            wageType = "WageType ",
            businessUnitKey = "BusinessUnitKey 2",
            businessUnit = "BusinessUnit ",
            parentTaskID = "ParentTaskID 2",
            prePlanningBoardQuickSelect = "PrePlanningBoardQuickSelect 2",
            colorCode = "#efef00",
            workingTime = "workingTime 2",
            isAvailableInTimeTrackingKioskMode = true,
        ),
    )

    override suspend fun requestResources(authorization: String): List<RemoteResourceJO>  =
        if (errorToReturn != null ){
            throw errorToReturn!!
        } else {
            resources
        }

    override suspend fun login(userCredentials: UserCredentials) = LoginResponse(
        oauth = OAuthJO(access_token = "authorization","Bearer")
    )
}

