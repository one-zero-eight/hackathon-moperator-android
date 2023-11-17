package ru.innopolis.moperator.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import ru.innopolis.moperator.MoperatorApplication


class LocationTracker(private val mContext: Context) {

    var locationByGPS: Location? = null
    var locationByNetwork: Location? = null


    // Declaring a Location Manager
    private var mLocationManager: LocationManager =
        mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Location", "No permissions")
        } else {
            setupLocationListeners()
        }
    }


    private fun onLocationChanged() {
        (mContext.applicationContext as MoperatorApplication).androidToWeb.onLocationChanged(
            location!!
        )
    }

    @SuppressLint("MissingPermission")
    fun setupLocationListeners() {
        // getting GPS status
        val hasGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // getting network status
        val hasNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!hasGPS && !hasNetwork) {
            // no network provider is enabled
            Log.d("Location", "No network provider is enabled")
        }

        if (hasGPS) {
            Log.d("Location", "GPS is enabled")
            // inline class
            val gpsListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationByGPS = location
                    Log.d("Location", "GPS location: $location")
                    onLocationChanged()
                }

                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}
            }

            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), gpsListener
            )
        }
        if (hasNetwork) {
            Log.d("Location", "Network is enabled")
            val networkListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationByNetwork = location
                    Log.d("Location", "Network location: $location")
                    onLocationChanged()
                }

                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}
            }

            mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), networkListener
            )
        }
    }

    val location: Location?
        get() {

            // Check locationByGPS and locationByNetwork
            if (locationByGPS == null && locationByNetwork == null) {
                return null
            }

            // If locationByGPS is null, return locationByNetwork
            if (locationByGPS == null) {
                return locationByNetwork
            }

            // If locationByNetwork is null, return locationByGPS
            if (locationByNetwork == null) {
                return locationByGPS
            }

            // If both are not null, return the one with accuracy
            // TODO: Also check for time
            return if (locationByGPS!!.accuracy < locationByNetwork!!.accuracy) {
                locationByGPS
            } else {
                locationByNetwork
            }
        }

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1 // 1 meter

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES = (1000 * 5).toLong() // 5 seconds
    }
}