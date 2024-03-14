package com.ljmarinscull.baubuddy.data

import com.ljmarinscull.baubuddy.data.datasource.local.AppPreferences
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PreferencesRepositoryFake: IPreferencesRepository {

    private val _preferencesFlow = MutableStateFlow(AppPreferences(authorization = ""))
    override val appPreferencesFlow = _preferencesFlow.asStateFlow()

    override suspend fun updateAuthorization(authorization: String) {
        _preferencesFlow.update { it.copy(authorization = authorization) }
    }

    override suspend fun fetchPreferences() = coroutineScope {
        AppPreferences("")
    }
}