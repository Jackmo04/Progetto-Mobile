package com.example.cacciaaltesoro.data.database.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDTO(
    @SerialName("tag_id")  val id: String,
    @SerialName("tag_posizione")  val number: Int,
    @SerialName("tag_partita") val eventId: Int,
    @SerialName("tag_hash") val hash: String,
    @SerialName("tag_latitudine") val lat: Double,
    @SerialName("tag_longitudine") val lon: Double,
    @SerialName("tag_indizio") val textHint: String,
    @SerialName("tag_immagine") val imageHint: String,

    @SerialName("partite")  val eventDTO : EventDTO? = null
)