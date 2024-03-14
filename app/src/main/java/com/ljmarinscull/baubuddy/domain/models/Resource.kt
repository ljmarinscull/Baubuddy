package com.ljmarinscull.baubuddy.domain.models

data class Resource(
    val task: String,
    val title: String,
    val description: String,
    val sort: String,
    val wageType: String,
    val businessUnitKey: String?,
    val businessUnit: String,
    val parentTaskID: String,
    val prePlanningBoardQuickSelect: String?,
    val colorCode: String,
    val workingTime: String?,
    val isAvailableInTimeTrackingKioskMode: Boolean
)