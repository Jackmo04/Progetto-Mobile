package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.database.dto.TagDTO
import com.example.cacciaaltesoro.data.database.dto.insert.TagInsertDTO
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.google.android.gms.maps.model.LatLng

fun Tag.toDto(): TagDTO {
    return TagDTO(
        id = id ?: throw IllegalArgumentException("Missing tag UUID"),
        number = number,
        eventId = eventId ?: throw IllegalArgumentException("Missing event id from tag"),
        lat = coordinates.latitude,
        lon = coordinates.longitude,
        textHint = textHint,
        imageHint = imageHint
    )
}

fun Tag.toInsertDto(): TagInsertDTO {
    return TagInsertDTO(
        number = number,
        eventId = eventId ?: throw IllegalArgumentException("Missing event id from tag"),
        lat = coordinates.latitude,
        lon = coordinates.longitude,
        textHint = textHint,
        imageHint = imageHint
    )
}

fun TagDTO.toDomain(): Tag {
    return Tag(
        id = id,
        number = number,
        eventId = eventId,
        coordinates = Coordinates(lat, lon),
        textHint = textHint,
        imageHint = imageHint
    )
}