package com.ljmarinscull.baubuddy.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteResourceJO(
    val task: String,
    val title: String,
    val description: String,
    val sort: String,
    val wageType: String,
    @SerialName("BusinessUnitKey") val businessUnitKey: String?,
    val businessUnit: String,
    val parentTaskID: String,
    @SerialName("preplanningBoardQuickSelect") val prePlanningBoardQuickSelect: String?,
    val colorCode: String,
    val workingTime: String?,
    val isAvailableInTimeTrackingKioskMode: Boolean
)

fun RemoteResourceJO.toResourceEntity() = ResourceEntity(
    task = task,
    title = title,
    description = description,
    sort = sort,
    wageType = wageType,
    businessUnitKey= businessUnitKey,
    businessUnit = businessUnit,
    parentTaskID = parentTaskID,
    prePlanningBoardQuickSelect = prePlanningBoardQuickSelect,
    colorCode = colorCode,
    workingTime = workingTime,
    isAvailableInTimeTrackingKioskMode = isAvailableInTimeTrackingKioskMode
)