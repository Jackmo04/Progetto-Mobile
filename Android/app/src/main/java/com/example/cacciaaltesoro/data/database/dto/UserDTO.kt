package com.example.cacciaaltesoro.data.database.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("utente")
data class UserDTO(
    @SerialName("ute_id") val uuid: String,
    @SerialName("ute_username") val username: String,
    @SerialName("ute_immagine") val image: String,
    @SerialName("partite") val eventDTOS: List<EventDTO> = emptyList(),
    @SerialName("tags") val tagDTOS: List<TagDTO> = emptyList()
)