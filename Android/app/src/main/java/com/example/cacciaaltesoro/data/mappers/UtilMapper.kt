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

fun List<Coordinates>.toLatLngList(): List<LatLng> {
    return map { it.toLatLng() }
}

fun List<LatLng>.toCoordinatesList(): List<Coordinates> {
    return map { it.toCoordinates() }
}

fun String.isUrl(): Boolean {
    return try {
        URL(this)
        true
    } catch (_: Exception) {
        false
    }
}