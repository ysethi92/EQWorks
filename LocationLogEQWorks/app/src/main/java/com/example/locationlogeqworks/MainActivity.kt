package com.example.locationlogeqworks

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eqworkslocationlibrary.Library
import com.example.eqworkslocationlibrary.LocationEvent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var sendLocation : Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val PERMISSION_REQUEST_ACCESS = 100
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendLocation = findViewById<Button>(R.id.sendLocation)
        val locationCoordinatesTextView = findViewById<TextView>(R.id.location)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermission()

        sendLocation.setOnClickListener {
            if(isLocationEnabled(this) && isNetworkAvailable()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) {
                    val location : Location?= it.result
                    if(location != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            check(LocationEvent(location.latitude.toFloat(), location.longitude.toFloat()), locationCoordinatesTextView)
                        }
                    }
                }
            } else Toast.makeText(this, "Check your internet and location", Toast.LENGTH_SHORT).show()
        }
    }

    private val noGPSDialog: AlertDialog by lazy {
        return@lazy AlertDialog.Builder(this)
            .setTitle(R.string.gps_not_found_title)
            .setMessage(R.string.gps_not_found_message)
            .setCancelable(false)
            .setPositiveButton(
                R.string.enable
            ) { _, _ ->
                this.startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                )
            }.create()
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayServices()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        }
    }

    private suspend fun check(locationEvent: LocationEvent, locationCoordinatesTextView : TextView) {
        val response = Library().log(locationEvent)
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                Log.d("Response", response.body()?.data.toString())

                val json = JSONObject(response.body()?.data.toString())

                val locationData = StringBuilder()
                locationData.append("Latitude = ")
                    .append(json.getDouble("latitude").toString())
                    .append("\n Longitude = ")
                    .append(json.getDouble("longitude").toString())
                    .append("\n Time = ")
                    .append(json.getLong("time").toString())

                locationCoordinatesTextView.text = locationData.toString()
            } else Toast.makeText(this@MainActivity, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askPermissions()
        }
        else {
            completeSetup()
        }
    }

    private fun completeSetup() {
        if (!isLocationEnabled(this)) {
            if (noGPSDialog.isShowing) Log.d(
                this.javaClass.simpleName,
                "Already showing"
            ) else noGPSDialog.show()
            return
        }
        sendLocation.isEnabled = true
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                completeSetup()
            } else {
                //check if permission denied permanently or not
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    askPermissions()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS
        )
    }

    private fun checkGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this)
        if(status != ConnectionResult.SUCCESS) {
            if(apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 1)?.show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Google Play services not available", Snackbar.LENGTH_INDEFINITE).show()
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}