package com.example.dops_location_app

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
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
import java.util.*

open class MainActivity : AppCompatActivity() {
    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationRequest = LocationRequest.create()?.apply {
        interval = 1000
        fastestInterval = 1000
        // smallestDisplacement = 100f // Set the minimum displacement between location updates in meters
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private var lastRequestTime = Calendar.getInstance()
    private var message = ""
    private var brightnessOff = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        // Brightness
        //window.attributes.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF

        //changeBrightness(false)

        fab.setOnClickListener{
            changeBrightness(brightnessOff)
            brightnessOff = !brightnessOff
        }
    }

    private fun changeBrightness(turnOn: Boolean){
        val lp = window.attributes
        if(turnOn)
            lp.screenBrightness = 1f
        else
            lp.screenBrightness = 0f

        window.attributes = lp
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

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(createPendingIntent())
        super.onDestroy()
    }

    private fun createLocationRequest() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { startLocationUpdates() }
        task.addOnFailureListener { exception -> onFailureListenerLocationSettings(exception) }
    }

    private fun createPendingIntent(): PendingIntent {
        val broadcastName = getString(R.string.location_broadcast_name)
        val intent = Intent(broadcastName)

        return PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    private fun getDifferenceTime(): String{
        var difTime = ""
        val current = Calendar.getInstance()

        val diff = current.time.time - lastRequestTime.time.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        if (lastRequestTime.before(current)) {
            difTime = "Difference: $days:$hours:$minutes:$seconds"
        }

        lastRequestTime = current

        return difTime
    }

    private fun startLocationUpdates() {
        // All location settings are satisfied. The client can initialize
        // location requests here.
        // ...
        printMessage("All location settings are satisfied. The client can initialize")
        if (ContextCompat.checkSelfPermission(this@MainActivity, fineLocationPermission) == PackageManager.PERMISSION_GRANTED) {
            printMessage("Permission $fineLocationPermission is granted")
            printMessage("")

            // With a Pending Intent
            fusedLocationClient.requestLocationUpdates(locationRequest, createPendingIntent())

            // With a Callback
//            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
//                override fun onLocationResult(locationResult: LocationResult?) {
//                    for (location in locationResult!!.locations)
//                        printMessage("[Lat:${location.latitude}]\t\t[Long:${location.longitude}]\t\t[${getDifferenceTime()}]")
//                }
//            }, null)
        } else {
            printMessage("Permission ${Manifest.permission.ACCESS_FINE_LOCATION} is not granted")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                this@MainActivity.requestPermissions(arrayOf(fineLocationPermission), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    private fun onFailureListenerLocationSettings(exception: Exception) {
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
