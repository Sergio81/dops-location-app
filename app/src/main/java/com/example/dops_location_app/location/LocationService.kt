package com.example.dops_location_app.location

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.dops_location_app.model.LocationResponse
import com.google.android.gms.location.*
import java.util.*

class LocationService(context: Context) {
    companion object {
        private const val TAG = "LocationServiceTAG"
    }

    private var tStart = System.currentTimeMillis()
    private var fusedLocationClient:FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val lastLocation = MutableLiveData<LocationResponse>()

    init{
        try {
            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    fusedLocationClient.lastLocation
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                task.result?.apply {
                                    val tEnd = System.currentTimeMillis()
                                    val tDelta = tEnd - tStart
                                    val elapsedSeconds = tDelta / 1000.0

                                    lastLocation.value = LocationResponse(
                                        latitude,
                                        longitude,
                                        elapsedSeconds
                                    )

                                    tStart = System.currentTimeMillis()
                                }
                            } else {
                                Log.w(TAG, "Failed to get location.")
                            }
                        }
                }
            }, 0, 1000)
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }
}