package com.example.dops_location_app

import android.Manifest
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dops_location_app.app.Constants
import com.example.dops_location_app.app.Constants.Companion.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.dops_location_app.model.LocationResponse
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
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import java.util.*

open class MainActivity : AppCompatActivity(){
    //region Global variables
    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    //private var lastRequestTime = Calendar.getInstance()
    private var brightnessOff = false
    private var minBrightness = 0f
    private var maxBrightness = 100f
    private var currentBrightness = 50f
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(bottom_app_bar)

        fab.setOnClickListener {
            changeBrightness(brightnessOff)
            brightnessOff = !brightnessOff
        }

        // Check that the user hasn't revoked permissions by going to Settings.
        //if (LocationService.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions()
            }
        //}

        setSeekBars()
    }

    private fun setSeekBars() {
        minSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtMinValue.text = "$progress%"
                minBrightness = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        maxSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtMaxValue.text = "$progress%"
                maxBrightness = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        currentSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtCurrentValue.text = "$progress%"
                currentBrightness = progress.toFloat()
                changeBrightness(currentBrightness)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        }

        mRecyclerView.adapter = LocationAdapter().apply { updateItems(createSampleData()) }
        mRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun createSampleData() : ArrayList<LocationResponse>{
        val items = ArrayList<LocationResponse>()

        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 5))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))
        items.add(LocationResponse(1231.34534f, 123.45f, 6))
        items.add(LocationResponse(1231.34534f, 123.45f, 4))
        items.add(LocationResponse(1231.34534f, 123.45f, 8))
        items.add(LocationResponse(1231.34534f, 123.45f, 7))

        return items
    }

    //region UI
    private fun changeBrightness(turnOn: Boolean) {
        val lp = window.attributes
        if (turnOn)
            lp.screenBrightness = maxBrightness/100
        else
            lp.screenBrightness = minBrightness/100

        window.attributes = lp
        currentBrightness = lp.screenBrightness * 100
        txtCurrentValue.text = "${currentBrightness.toInt()}%"
        currentSeekBar.progress = currentBrightness.toInt()
    }

    private fun changeBrightness(value: Float) {
        val lp = window.attributes
            lp.screenBrightness = value/100

        window.attributes = lp
        currentBrightness = value
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i("myTag", "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> // Permission was granted.
                    //mService!!.requestLocationUpdates()
                    TODO("Get location updates")
                else -> {
                    // Permission denied.
                    //setButtonsState(false)
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        "Permission was denied, but is needed for core functionality",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.settings, View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                        .show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.app_bar_fav -> toast("fab button")
            R.id.app_bar_search -> toast("search")
            R.id.app_bar_settings -> toast("settings")
        }
        return true
    }

    // This is an extension method for easy Toast call
    private fun Context.toast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 325)
        toast.show()
    }
    //endregion

    //region Permissions
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            //Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.coordinatorLayout),
                "Location permission rationale to provide additional content",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            //Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
    //endregion

}
