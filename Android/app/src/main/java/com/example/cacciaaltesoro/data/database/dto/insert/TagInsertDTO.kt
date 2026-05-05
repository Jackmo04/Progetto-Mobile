package com.example.cacciaaltesoro.data.database.dto.insert

import com.example.cacciaaltesoro.data.database.dto.EventDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagInsertDTO(
    @SerialName("tag_posizione")  val number: Int,
    @SerialName("tag_partita") val eventId: Int,
    @SerialName("tag_latitudine") val lat: Double,
    @SerialName("tag_longitudine") val lon: Double,
    @SerialName("tag_indizio") val textHint: String?,
    @SerialName("tag_immagine") val imageHint: String?
)