package com.ljmarinscull.baubuddy.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.util.EMPTY_STRING_PLACEHOLDER
import com.ljmarinscull.baubuddy.util.WHITE_COLOR_HEX

@Entity(
    tableName = "resources",
    primaryKeys = ["task"]
)
data class ResourceEntity(
    @ColumnInfo(name = "task") val task: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "sort") val sort: String,
    @ColumnInfo(name = "wageType") val wageType: String,
    @ColumnInfo(name = "businessUnitKey") val businessUnitKey: String?,
    @ColumnInfo(name = "businessUnit") val businessUnit: String,
    @ColumnInfo(name = "parentTaskID") val parentTaskID: String,
    @ColumnInfo(name = "prePlanningBoardQuickSelect") val prePlanningBoardQuickSelect: String?,
    @ColumnInfo(name = "colorCode") val colorCode: String,
    @ColumnInfo(name = "workingTime") val workingTime: String?,
    @ColumnInfo(name = "isAvailableInTimeTrackingKioskMode") val isAvailableInTimeTrackingKioskMode: Boolean
)

fun ResourceEntity.toResource() = Resource(
    task = task,
    title = title,
    description = description.ifEmpty { EMPTY_STRING_PLACEHOLDER },
    sort = sort,
    wageType = wageType,
    businessUnitKey = businessUnitKey,
    businessUnit = businessUnit,
    parentTaskID = parentTaskID,
    prePlanningBoardQuickSelect = prePlanningBoardQuickSelect,
    colorCode = colorCode.ifEmpty { WHITE_COLOR_HEX },
    workingTime = workingTime,
    isAvailableInTimeTrackingKioskMode = isAvailableInTimeTrackingKioskMode
)