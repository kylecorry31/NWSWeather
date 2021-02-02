package com.kylecorry.nwsweather

import android.content.Context
import android.text.format.DateUtils
import java.time.Instant

class FormatService(private val context: Context) {

    fun formatDateTime(instant: Instant): String {
        return DateUtils.formatDateTime(
            context, instant.toEpochMilli(),
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME
        )
    }
}