package com.ljmarinscull.baubuddy.data.datasource.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

data class AppPreferences(
    val authorization: String
)

interface IPreferencesRepository{
    val appPreferencesFlow: Flow<AppPreferences>
    suspend fun updateAuthorization(authorization: String)
    suspend fun fetchPreferences(): AppPreferences
}

class PreferencesRepository(private val dataStore: DataStore<Preferences>) : IPreferencesRepository {

    private val TAG: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val AUTHORIZATION = stringPreferencesKey("authorization")
    }

    override val appPreferencesFlow: Flow<AppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapAppPreferences(preferences)
        }

    override suspend fun updateAuthorization(authorization: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTHORIZATION] = authorization
        }
    }

    override suspend fun fetchPreferences() =
        mapAppPreferences(dataStore.data.first().toPreferences())

    private fun mapAppPreferences(preferences: Preferences): AppPreferences {
        val authorization = preferences[PreferencesKeys.AUTHORIZATION] ?: ""
        return AppPreferences(authorization = authorization)
    }
}