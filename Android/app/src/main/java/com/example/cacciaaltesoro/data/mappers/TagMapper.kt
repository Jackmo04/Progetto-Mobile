package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.database.dto.TagDTO
import com.example.cacciaaltesoro.data.domain.Tag
import com.google.android.gms.maps.model.LatLng

fun Tag.toDto(): TagDTO {
    return TagDTO(
        id = id,
        number = number,
        eventId = eventId,
        hash = hash,
        lat = latLng.latitude,
        lon = latLng.longitude,
        textHint = textHint,
        imageHint = imageHint
    )
}

fun TagDTO.toDomain(): Tag {
    return Tag(
        id = id,
        number = number,
        eventId = eventId,
        hash = hash,
        latLng = LatLng(lat, lon),
        textHint = textHint,
        imageHint = imageHint
    )
}