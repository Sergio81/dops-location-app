package com.example.dops_location_app

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.res.Configuration
import android.os.*
import android.preference.PreferenceManager
import android.util.Log

class LocationService : Service() {
    //region Global variables
    companion object {
        private const val PACKAGE_NAME = "com.example.dops_location_app"

        const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates"

        private const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

        private val TAG = LocationService::class.java.simpleName
        private const val NOTIFICATION_ID = 12345678
        private const val CHANNEL_ID = "channel_01"

        fun requestingLocationUpdates(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
        }
    }

    private val mBinder = LocalBinder()
    private var mLocation: Location? = null
    private var mChangingConfiguration = false
    private var mServiceHandler: Handler? = null
    private var mLocationCallback: LocationCallback? = null
    private var mNotificationManager: NotificationManager? = null
    private val locationRequest = LocationRequest.create()?.apply {
        interval = 1000
        fastestInterval = 1000
        // smallestDisplacement = 100f // Set the minimum displacement between location updates in meters
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //endregion

    //region Overrides
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }

        getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startedFromNotification = intent!!.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return Service.START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!mChangingConfiguration && requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service")

            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
    //endregion

    //region New functions
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationService::class.java))
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            setRequestingLocationUpdates(this, false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }

    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLocation = task.result
                    } else {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }

    }

    private fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
            setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            setRequestingLocationUpdates(this, true)
            Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }

    }

    private fun onNewLocation(location: Location) {
        //Log.i(TAG, "New location: $location")

        mLocation = location

        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager!!.notify(NOTIFICATION_ID, getNotification())
        }
    }

    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }

        return false
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, LocationService::class.java)

        val text = getLocationText(mLocation)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), 0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager!!.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "MyChanelName",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val builder = NotificationCompat.Builder(this, "default")
            .addAction(
                R.drawable.baseline_search_white_24, "Title: ",
                activityPendingIntent
            )
            .addAction(
                R.drawable.baseline_favorite_white_24, "Title: ",
                servicePendingIntent
            )
            .setContentText(text)
            .setContentTitle("Content Title")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())


        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        return builder.build()
    }

    private fun getLocationText(location: Location?): String {
        return if (location == null)
            "Unknown location"
        else
            "(" + location.latitude + ", " + location.longitude + ")"
    }


    private fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
    //endregion
}