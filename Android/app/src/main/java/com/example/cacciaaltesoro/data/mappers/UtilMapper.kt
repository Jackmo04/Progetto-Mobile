package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.google.android.gms.maps.model.LatLng
import java.net.URL

fun Coordinates.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toCoordinates(): Coordinates {
    return Coordinates(latitude, longitude)
}

fun String.isUrl(): Boolean {
    return try {
        URL(this)
        true
    } catch (_: Exception) {
        false
    }
}