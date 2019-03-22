package com.example.dops_location_app

import com.google.android.gms.location.LocationResult
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            val location = locationResult.lastLocation
            if (location != null) {
                // Do something with your location
                Log.d("LOCATION_RESULT", "[Lat:${location.latitude}]\t[Long:${location.longitude}]")
            } else {
                Log.d("LOCATION_RESULT", "*** location object is null ***")
            }
        }
    }

}