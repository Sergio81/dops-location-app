package com.example.dops_location_app.app

import android.app.Application
import android.app.Service
import com.example.dops_location_app.location.LocationService

class LocationApp : Application() {
    companion object {
        lateinit var app : LocationApp
    }
    //private lateinit var locationService: LocationService

    override fun onCreate() {
        super.onCreate()
        app = this
        //locationService = LocationService(this)
    }
}