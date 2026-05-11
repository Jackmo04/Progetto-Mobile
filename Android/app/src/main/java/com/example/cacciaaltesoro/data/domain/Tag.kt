package com.example.cacciaaltesoro.data.domain

import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import java.util.UUID

data class Tag (
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val eventId: Int? = null,
    val coordinates: Coordinates,
    val textHint: String? = null,
    val imageHint: String? = null
)


