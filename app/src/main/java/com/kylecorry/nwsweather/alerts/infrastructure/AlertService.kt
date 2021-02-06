package com.kylecorry.nwsweather.alerts.infrastructure

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.kylecorry.nationalweatherservice.Alert
import com.kylecorry.nationalweatherservice.NationalWeatherServiceProxy
import com.kylecorry.nwsweather.FormatService
import com.kylecorry.nwsweather.R
import com.kylecorry.nwsweather.alerts.domain.AlertUtils
import com.kylecorry.nwsweather.infrastructure.CustomGPS
import com.kylecorry.trailsensecore.infrastructure.system.IntentUtils
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils
import com.kylecorry.trailsensecore.infrastructure.system.PowerUtils
import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

class AlertService : Service() {

    private val gps by lazy { CustomGPS(applicationContext) }
    private val nwsService by lazy { NationalWeatherServiceProxy(getString(R.string.nws_user_agent)) }
    private val formatService by lazy { FormatService(applicationContext) }

    private var wakelock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Started at ${ZonedDateTime.now()}")
        acquireWakelock()
        scheduleNextUpdate()

        val alerts = runBlocking {
            withContext(Dispatchers.IO) {
                nwsService.getAlerts(gps.location.latitude, gps.location.longitude)
            }
        }

        if (alerts.isNotEmpty()) {
            notifyAlerts(alerts)
        }

        wrapUp()

        return START_NOT_STICKY
    }

    private fun notifyAlerts(alerts: List<Alert>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val lastIds = prefs.getStringSet("cache_last_alerts", null) ?: listOf()
        val currentIds = alerts.map { it.id }
        alerts.filter { !lastIds.contains(it.id) }.forEachIndexed { index, alert ->
            val name = alert.event
            val start = AlertUtils.getStart(alert)
            val end = AlertUtils.getEnd(alert)
            val timeString = formatService.formatDateTimeRange(start, end)
            val severity = formatService.formatSeverity(alert.severity)

            val notification = NotificationUtils.builder(applicationContext, "WeatherAlerts")
                .setContentTitle(name)
                .setContentText("$timeString [$severity]")
                .setSmallIcon(R.drawable.alert)
                .build()

            NotificationUtils.send(applicationContext, 128321 + index, notification)
        }
        prefs.edit {
            putStringSet("cache_last_alerts", currentIds.toSet())
        }
    }

    private fun scheduleNextUpdate() {
        val scheduler = AlertScheduler.getScheduler(applicationContext)
        scheduler.cancel()
        scheduler.schedule(Duration.ofMinutes(30))
    }

    private fun releaseWakelock() {
        try {
            if (wakelock?.isHeld == true) {
                wakelock?.release()
            }
        } catch (e: Exception) {
            // DO NOTHING
        }
    }

    private fun acquireWakelock() {
        try {
            wakelock = PowerUtils.getWakelock(applicationContext, TAG)
            releaseWakelock()
            wakelock?.acquire(60 * 1000L)
        } catch (e: Exception) {
            // DO NOTHING
        }
    }


    override fun onDestroy() {
        wrapUp()
        super.onDestroy()
    }

    private fun wrapUp() {
        releaseWakelock()
        stopForeground(true)
        stopSelf()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    companion object {

        private const val TAG = "AlertService"

        fun intent(context: Context): Intent {
            return Intent(context, AlertService::class.java)
        }

        fun start(context: Context) {
            IntentUtils.startService(context, intent(context), foreground = false)
        }
    }
}