package com.example.cacciaaltesoro.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class LocationService(private val ctx: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
    private val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _coordinates = MutableStateFlow<Coordinates?>(null)
    val coordinates = _coordinates.asStateFlow()

    suspend fun getCurrentLocation(usePreciseLocation: Boolean = true): Coordinates? {
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!locationEnabled) throw IllegalStateException("Location is disabled")

        val permissionGranted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) throw SecurityException("Location permission not granted")

        _coordinates.value = fusedLocationClient.getCurrentLocation(
            if (usePreciseLocation) Priority.PRIORITY_HIGH_ACCURACY
            else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).await()?.toCoordinates()

        return coordinates.value
    }

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)
}
