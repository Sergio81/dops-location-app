package com.example.dops_location_app.app

import android.app.Application

class LocationApp : Application() {
    companion object {
        lateinit var app : LocationApp
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}