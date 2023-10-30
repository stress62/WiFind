package com.example.wifind.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifind.R
import com.example.wifind.WifiCardAdapter
import com.example.wifind.model.Wifi
import com.example.wifind.model.WifiCard
import com.parse.ParseQuery


class MarketBoardActivity : AppCompatActivity() {

    private lateinit var wifiRecyclerView: RecyclerView
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_board)

        wifiRecyclerView = findViewById(R.id.recycler_view)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val wifis = getAllWifis()

        getCurrentLocation { location ->
            wifiRecyclerView.adapter = WifiCardAdapter(
                wifiCards = wifis.map { it.toWifiCard(location) }
            )
            wifiRecyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    fun getAllWifis(): List<Wifi> {
        return ParseQuery.getQuery<Wifi>("Wifi").find()
    }

    fun Wifi.toWifiCard(currentLocation: Location): WifiCard {
        val wifi = this
        val wifiLocation = Location("wifiLocation").apply {
            latitude = wifi.location.latitude
            longitude = wifi.location.longitude
        }
        return WifiCard(
            wifi = this,
            distanceToWifi = currentLocation.distanceTo(wifiLocation).toDouble()
        )
    }

    @SuppressLint("MissingPermission") // already doing check
    private fun getCurrentLocation(block: (Location) -> Unit) {
        if (isLocationPermissionGranted()) {
            locationManager.getCurrentLocation(
                /* provider = */ LocationManager.NETWORK_PROVIDER,
                /* cancellationSignal = */ null,
                /* executor = */ { it.run() },
                /* consumer = */ { block(it) }
            )
        }
    }

    private fun isLocationPermissionGranted(): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}