package com.ljmarinscull.baubuddy.ui.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.ljmarinscull.baubuddy.data.datasource.local.PreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.local.ResourcesDao
import com.ljmarinscull.baubuddy.data.datasource.remote.ApiService

class RefreshResourcesWorkerFactory(
    private val api: ApiService,
    private val preferencesRepository: PreferencesRepository,
    private val resourcesDao: ResourcesDao
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        return when (workerClassName) {
            RefreshResourcesWorker::class.java.name ->
                RefreshResourcesWorker(
                    appContext,
                    workerParameters,
                    api,
                    preferencesRepository,
                    resourcesDao
                )
            else ->
                null
        }
    }
}