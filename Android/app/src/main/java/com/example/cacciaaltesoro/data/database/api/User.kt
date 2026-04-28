package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("utente")
data class User(
    @SerialName("ute_id") val uuid: String? = null,
    @SerialName("ute_username") val username: String? = null,
    @SerialName("ute_immagine") val image: String? = null,
    @SerialName("partite") val events: List<Event> = emptyList(),
    @SerialName("tags") val tags: List<Tag> = emptyList()
)