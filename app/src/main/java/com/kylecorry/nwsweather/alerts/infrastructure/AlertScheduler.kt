package com.kylecorry.nwsweather.alerts.infrastructure

import android.content.Context
import com.kylecorry.trailsensecore.infrastructure.tasks.ITaskScheduler
import java.time.Duration

object AlertScheduler {
    fun start(context: Context) {
        val scheduler = getScheduler(context)
        scheduler.schedule(Duration.ZERO)
    }

    fun stop(context: Context) {
        val scheduler = getScheduler(context)
        scheduler.cancel()
        context.stopService(AlertService.intent(context))
    }

    fun getScheduler(context: Context): ITaskScheduler {
        return AlertWorker.scheduler(context)
    }
}