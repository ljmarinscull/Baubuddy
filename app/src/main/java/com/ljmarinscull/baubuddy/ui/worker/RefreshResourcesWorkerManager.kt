package com.ljmarinscull.baubuddy.ui.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class RefreshResourcesWorkerManager(private val workManager: WorkManager) : IWorkerManager {
     private fun createWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequest.Builder(RefreshResourcesWorker::class.java, 60, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10L, TimeUnit.SECONDS)
            .build()
    }

    override fun enqueueServicesUpdate() {
        val workerRequest = createWorkRequest()
        workManager
            .enqueueUniquePeriodicWork(
                RefreshResourcesWorker.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workerRequest
            )
    }
}