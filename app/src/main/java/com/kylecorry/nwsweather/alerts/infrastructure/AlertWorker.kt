package com.kylecorry.nwsweather.alerts.infrastructure

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kylecorry.trailsensecore.infrastructure.tasks.DeferredTaskScheduler
import com.kylecorry.trailsensecore.infrastructure.tasks.ITaskScheduler

class AlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        AlertService.start(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "com.kylecorry.nwsweather.AlertWorker"

        fun scheduler(context: Context): ITaskScheduler {
            return DeferredTaskScheduler(
                context,
                AlertWorker::class.java,
                WORK_TAG)
        }
    }

}