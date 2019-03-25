package com.example.dops_location_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location

class MyReceiver2 : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location: Location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION)

//        Toast.makeText(
//            this@MainActivity, Utils.getLocationText(location),
//            Toast.LENGTH_SHORT
//        ).show()
    }
}
