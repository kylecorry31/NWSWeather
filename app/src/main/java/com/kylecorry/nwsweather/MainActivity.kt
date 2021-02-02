package com.kylecorry.nwsweather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kylecorry.nwsweather.alerts.infrastructure.AlertScheduler
import com.kylecorry.trailsensecore.infrastructure.system.NotificationUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragment_holder) as NavHostFragment).navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setupWithNavController(navController)

        NotificationUtils.createChannel(
            this,
            "WeatherAlerts",
            "Weather Alerts",
            "Weather Alerts",
            NotificationUtils.CHANNEL_IMPORTANCE_HIGH,
            false
        )

        AlertScheduler.start(applicationContext)
    }

}