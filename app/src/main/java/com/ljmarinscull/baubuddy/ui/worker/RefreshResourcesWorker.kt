package com.ljmarinscull.baubuddy.ui.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ljmarinscull.baubuddy.data.datasource.local.IPreferencesRepository
import com.ljmarinscull.baubuddy.data.datasource.local.ResourcesDao
import com.ljmarinscull.baubuddy.data.datasource.remote.ApiService
import com.ljmarinscull.baubuddy.data.models.toResourceEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RefreshResourcesWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val api: ApiService,
    private val preferencesRepository: IPreferencesRepository,
    private val resourcesDao: ResourcesDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val result = async {
            val authorization = preferencesRepository.fetchPreferences().authorization
            if (authorization.isNotEmpty()) {
                val result = api.requestResources(authorization)
                val entities = result.map { it.toResourceEntity() }
                resourcesDao.save(entities)
            }
        }
        result.await()
        Result.success()
    }

    companion object {
        const val PERIODIC_WORK_NAME = "RefreshResourcesWorker"
    }
}