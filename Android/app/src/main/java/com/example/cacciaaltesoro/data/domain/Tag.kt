package com.example.cacciaaltesoro.data.domain

import com.example.cacciaaltesoro.data.domain.utils.Coordinates

data class Tag (
    val id: String? = null,
    val number: Int,
    val eventId: Int? = null,
    val coordinates: Coordinates,
    val textHint: String?,
    val imageHint: String?
)


