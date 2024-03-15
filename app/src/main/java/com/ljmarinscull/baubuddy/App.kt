package com.ljmarinscull.baubuddy

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import com.ljmarinscull.baubuddy.data.datasource.local.AppDatabase
import com.ljmarinscull.baubuddy.data.datasource.local.PreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.remote.RetrofitClient
import com.ljmarinscull.baubuddy.ui.worker.RefreshResourcesWorkerFactory

private const val USER_PREFERENCES_NAME = "app_preferences"

val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)
class App : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(RefreshResourcesWorkerFactory(
                RetrofitClient.getInstance(),
                PreferencesRepository(dataStore),
                AppDatabase.getInstance(this).resourcesDao()
            ))
            .build()
}