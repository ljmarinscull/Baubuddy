package com.ljmarinscull.baubuddy.data.datasource.local

import kotlinx.coroutines.flow.Flow

data class AppPreferences(
    val authorization: String
)

interface IPreferencesRepository{
    val appPreferencesFlow: Flow<AppPreferences>
    suspend fun updateAuthorization(authorization: String)
    suspend fun fetchPreferences(): AppPreferences
}