package com.example.cacciaaltesoro.data.domain

import com.example.cacciaaltesoro.utils.Coordinates
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Event (
    val id: Int?,
    val name: String,
    val organizerUUID: String,
    val lat: Double,
    val lon: Double,
    val location: Coordinates = Coordinates(lat, lon),
    val startTime: Instant,
    val endTime: Instant,
    val description: String?,
    val code: String,
    val isPrivate: Boolean,
    val tags: List<Tag>,
    val organizer: User? = null
)