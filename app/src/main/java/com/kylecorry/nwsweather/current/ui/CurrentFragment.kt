package com.kylecorry.nwsweather.current.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kylecorry.nationalweatherservice.Forecast
import com.kylecorry.nationalweatherservice.NationalWeatherServiceProxy
import com.kylecorry.nationalweatherservice.Observation
import com.kylecorry.nwsweather.R
import com.kylecorry.nwsweather.databinding.*
import com.kylecorry.nwsweather.infrastructure.CustomGPS
import com.kylecorry.trailsensecore.domain.geo.Bearing
import com.kylecorry.trailsensecore.domain.math.roundPlaces
import com.kylecorry.trailsensecore.domain.units.Distance
import com.kylecorry.trailsensecore.domain.units.DistanceUnits
import com.kylecorry.trailsensecore.domain.weather.PressureAltitudeReading
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import kotlin.math.roundToInt

class CurrentFragment : Fragment() {

    private var _binding: FragmentCurrent2Binding? = null
    private val binding get() = _binding!!

    private val gps by lazy { CustomGPS(requireContext()) }
    private val nwsService by lazy { NationalWeatherServiceProxy(getString(R.string.nws_user_agent)) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrent2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            val observations = withContext(Dispatchers.IO) {
                nwsService.getLatestObservations(gps.location.latitude, gps.location.longitude)
            }

            observations?.let {
                withContext(Dispatchers.Main) {
                    if (it.temperature == null){
                        binding.temperature.text = "-"
                    } else {
                        binding.temperature.text = "${(it.temperature!! * 9/5f + 32).roundToInt()}°F"
                    }

                    if (it.windChill == null){
                        binding.feelsLike.text = "Feels like ${((it.temperature ?: 0f) * 9/5f + 32).roundToInt()}°F"
                    } else {
                        val chill = (it.windChill!! * 9/5f + 32).roundToInt()
                        binding.feelsLike.text = "Feels like $chill°F"
                    }

                    binding.temperatureAlert.visibility = View.INVISIBLE

                    if (it.relativeHumidity == null){
                        binding.humidityValue.text = "-"
                    } else {
                        binding.humidityValue.text = "${it.relativeHumidity!!.roundToInt()}%"
                    }

                    if (it.dewpoint == null){
                        binding.dewpointValue.text = "-"
                    } else {
                        binding.dewpointValue.text = "${(it.dewpoint!! * 9/5f + 32).roundToInt()}°F"
                    }

                    if (it.seaLevelPressure != null) {
                        binding.pressureValue.text = "${(it.seaLevelPressure!! / 100f).roundPlaces(2)} hPa"
                    } else if (it.barometricPressure != null && it.elevation != null){
                        val seaLevel = PressureAltitudeReading(Instant.now(), it.barometricPressure!! / 100f, it.elevation!!, it.temperature ?: 0f).seaLevel(true)
                        binding.pressureValue.text = "${seaLevel.value.roundPlaces(2)} hPa"
                    } else if (it.barometricPressure != null){
                        binding.pressureValue.text = "${(it.barometricPressure!! / 100f).roundPlaces(2)} hPa"
                    } else {
                        binding.pressureValue.text = "-"
                    }

                    if (it.visibility == null){
                        binding.visibilityValue.text = "-"
                    } else {
                        val distance = Distance(it.visibility!!, DistanceUnits.Meters).convertTo(DistanceUnits.Miles)
                        binding.visibilityValue.text = "${(distance.distance).roundToInt()} mi"
                    }

                    if (it.windSpeed != null){
                        binding.windValue.text = "${(it.windSpeed!! * 2.23693629).roundToInt()} mph"
                    } else {
                        binding.windValue.text = "-"
                    }

                }
            }

        }
        return false
    }


}