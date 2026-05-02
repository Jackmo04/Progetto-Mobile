@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.Event
import kotlin.time.ExperimentalTime

fun Event.toDto(): EventDTO {
    return EventDTO(
        id = id,
        name = name,
        organizerUUID = organizerUUID,
        lat = lat,
        lon = lon,
        startTime = startTime,
        endTime = endTime,
        description = description,
        code = code,
        isPrivate = isPrivate
    )
}
fun EventDTO.toDomain(): Event = Event(
    id = id ?: throw IllegalArgumentException("Missing Event ID"),
    name = name,
    organizerUUID = organizerUUID,
    lat = lat,
    lon = lon,
    startTime = startTime,
    endTime = endTime,
    description = description,
    code = code,
    isPrivate = isPrivate,
    organizer = userDTO?.toDomain()
)
