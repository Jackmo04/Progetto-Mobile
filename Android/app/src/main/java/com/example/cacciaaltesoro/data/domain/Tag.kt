package com.example.cacciaaltesoro.data.domain

import com.example.cacciaaltesoro.data.domain.utils.Coordinates

data class Tag (
    val id: String?,
    val number: Int,
    val eventId: Int?,
    val coordinates: Coordinates,
    val textHint: String?,
    val imageHint: String?
) {
    constructor(number: Int, coordinates: Coordinates, textHint: String?, imageHint: String?) : this(
        id = null,
        number = number,
        eventId = null,
        coordinates = coordinates,
        textHint = textHint,
        imageHint = imageHint
    )
}


