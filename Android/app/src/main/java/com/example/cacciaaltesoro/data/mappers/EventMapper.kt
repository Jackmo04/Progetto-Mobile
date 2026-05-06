@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.insert.EventInsertDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import kotlin.time.ExperimentalTime

fun Event.toDto(): EventDTO {
    return EventDTO(
        id = id ?: throw IllegalArgumentException("Missing Event ID"),
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

fun Event.toInsertDto(): EventInsertDTO {
    return EventInsertDTO(
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

fun EventDTO.toDomain(tags: List<Tag>? = null): Event = Event(
    id = id,
    name = name,
    organizerUUID = organizerUUID,
    lat = lat,
    lon = lon,
    startTime = startTime,
    endTime = endTime,
    description = description,
    code = code,
    isPrivate = isPrivate,
    tags = tags ?: emptyList(),
    organizer = userDTO?.toDomain()
)
