package com.example.cacciaaltesoro.data.domain

import com.google.android.gms.maps.model.LatLng

data class Tag (
    val id: String?,
    val number: Int,
    val eventId: Int,
    val hash: String,
    val latLng: LatLng,
    val textHint: String,
    val imageHint: String
)

