package com.kylecorry.nwsweather.forecast

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kylecorry.nationalweatherservice.Forecast
import com.kylecorry.nationalweatherservice.NationalWeatherServiceProxy
import com.kylecorry.nwsweather.R
import com.kylecorry.nwsweather.databinding.FragmentForecastBinding
import com.kylecorry.nwsweather.databinding.ListItemForecastBinding
import com.kylecorry.nwsweather.infrastructure.CustomGPS
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private val gps by lazy { CustomGPS(requireContext()) }
    private val nwsService by lazy { NationalWeatherServiceProxy(getString(R.string.nws_user_agent)) }

    private lateinit var listView: ListView<Forecast>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = ListView(
            binding.forecastList,
            R.layout.list_item_forecast
        ) { forecastView, forecast ->
            val itemBinding = ListItemForecastBinding.bind(forecastView)
            itemBinding.forecastDate.text = forecast.name

            itemBinding.forecastDescription.text = forecast.shortForecast
            forecastView.setOnClickListener {
                UiUtils.alert(
                    requireContext(),
                    forecast.name,
                    forecast.shortForecast + "\n\n" + forecast.longForecast,
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
            val forecast = withContext(Dispatchers.IO) {
                nwsService.getForecast(gps.location.latitude, gps.location.longitude)
            }
            withContext(Dispatchers.Main) {
                listView.setData(forecast)
            }
        }
        return false
    }


}