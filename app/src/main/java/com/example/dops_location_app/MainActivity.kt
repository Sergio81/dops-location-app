package com.example.dops_location_app

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest
import java.lang.Exception

open class MainActivity : AppCompatActivity() {
    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequest = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    createLocationRequest()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finishAffinity()
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun createLocationRequest() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { onSuccessListener() }

        task.addOnFailureListener { exception -> onFailureListener(exception) }
    }

    private fun onSuccessListener() {
        // All location settings are satisfied. The client can initialize
        // location requests here.
        // ...
        printMessage("All location settings are satisfied. The client can initialize")
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                fineLocationPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            printMessage("Permission $fineLocationPermission is granted")
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    for (location in locationResult!!.locations) {
                        printMessage("[Lat:${location.latitude}][Long:${location.longitude}]")
                    }
                }
            }, null)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this@MainActivity.requestPermissions(
                    arrayOf(fineLocationPermission),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            }
            printMessage("Permission ${Manifest.permission.ACCESS_FINE_LOCATION} is not granted")
        }
    }

    private fun onFailureListener(exception: Exception) {
        if (exception is ResolvableApiException) {
            // Location settings are not satisfied, but this can be fixed
            // by showing the user a dialog.
            try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                exception.startResolutionForResult(this@MainActivity, 2001)
                printMessage("Error: ${exception.message}, ${exception.resolution}")
                printMessage("Location settings are not satisfied!!!, but this can be fixed")
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error.
            }
        }
    }

    private fun printMessage(message: String) {
        this.message = "$message\n${this.message}"

        txtMessage.text = this.message
    }
}
