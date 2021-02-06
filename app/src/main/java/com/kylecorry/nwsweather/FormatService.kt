package com.kylecorry.nwsweather

import android.content.Context
import android.text.format.DateUtils
import com.kylecorry.nationalweatherservice.AlertSeverity
import java.time.Instant

class FormatService(private val context: Context) {

    fun formatDateTime(instant: Instant): String {
        return DateUtils.formatDateTime(
            context, instant.toEpochMilli(),
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
        )
    }

    fun formatDateTimeRange(start: Instant, end: Instant, now: Instant = Instant.now()): String {
        if (now >= start) {
            val startTime = context.getString(R.string.now)
            val endTime = formatDateTime(end)
            return "$startTime - $endTime"
        }

        return DateUtils.formatDateRange(
            context,
            start.toEpochMilli(),
            end.toEpochMilli(),
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
        )
    }

    fun formatSeverity(severity: AlertSeverity): String {
        return when(severity){
            AlertSeverity.Unknown -> context.getString(R.string.unknown)
            AlertSeverity.Minor -> context.getString(R.string.minor)
            AlertSeverity.Moderate -> context.getString(R.string.moderate)
            AlertSeverity.Severe -> context.getString(R.string.severe)
            AlertSeverity.Extreme -> context.getString(R.string.extreme)
        }
    }

}