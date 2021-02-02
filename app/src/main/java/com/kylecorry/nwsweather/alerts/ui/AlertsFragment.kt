package com.kylecorry.nwsweather.alerts.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kylecorry.nationalweatherservice.Alert
import com.kylecorry.nationalweatherservice.NationalWeatherServiceProxy
import com.kylecorry.nwsweather.FormatService
import com.kylecorry.nwsweather.R
import com.kylecorry.nwsweather.databinding.FragmentAlertsBinding
import com.kylecorry.nwsweather.databinding.ListItemAlertBinding
import com.kylecorry.nwsweather.infrastructure.CustomGPS
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class AlertsFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val gps by lazy { CustomGPS(requireContext()) }

    private val nwsService by lazy {  NationalWeatherServiceProxy(getString(R.string.nws_user_agent)) }
    private lateinit var listView: ListView<Alert>

    private val formatService by lazy { FormatService(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = ListView(
            binding.alertList,
            R.layout.list_item_alert
        ) { alertView, alert ->
            val itemBinding = ListItemAlertBinding.bind(alertView)
            itemBinding.alertName.text = alert.event
            val start = alert.onset.toInstant()
            val end = alert.ends.toInstant()
            val now = Instant.now()

            itemBinding.alertTimes.text =
                "${if (now > start) "Now" else formatService.formatDateTime(start)} - ${
                    formatService.formatDateTime(end)
                }"
            alertView.setOnClickListener {
                UiUtils.alert(
                    requireContext(),
                    alert.event,
                    alert.headline + "\n\n\n" + alert.description,
                    getString(R.string.dialog_ok)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isLocationEnabled(requireContext())) {
            if (gps.hasValidReading) {
                onLocationUpdate()
            } else {
                gps.start(this::onLocationUpdate)
            }
        } else {
            PermissionUtils.requestPermissions(
                requireActivity(),
                listOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123
            )
        }
    }

    override fun onPause() {
        super.onPause()
        gps.stop(this::onLocationUpdate)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.isLocationEnabled(requireContext())) {
            if (gps.hasValidReading) {
                onLocationUpdate()
            } else {
                gps.start(this::onLocationUpdate)
            }
        }
    }

    private fun onLocationUpdate(): Boolean {
        lifecycleScope.launch {
            val alerts = withContext(Dispatchers.IO) {
                nwsService.getAlerts(gps.location.latitude, gps.location.longitude)
            }
            withContext(Dispatchers.Main) {
                listView.setData(alerts)
            }
        }
        return false
    }

}