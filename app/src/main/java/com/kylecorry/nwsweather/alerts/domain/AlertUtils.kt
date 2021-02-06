package com.kylecorry.nwsweather.alerts.domain

import com.kylecorry.nationalweatherservice.Alert
import java.time.Instant

object AlertUtils {

    fun getStart(alert: Alert): Instant {
        return alert.onset?.toInstant() ?: alert.effective?.toInstant() ?: alert.sent?.toInstant() ?: Instant.now()
    }

    fun getEnd(alert: Alert): Instant {
        return alert.ends?.toInstant() ?: alert.expires?.toInstant() ?: Instant.now()
    }

}