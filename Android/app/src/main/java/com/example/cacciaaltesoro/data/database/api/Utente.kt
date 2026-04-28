package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Utente(
    @SerialName("ute_id") val uuid: String? = null,
    @SerialName("ute_username") val username: String? = null,
    @SerialName("ute_password") val ute_password: String? = null, // TODO remove this
    @SerialName("ute_immagine") val image: String? = null,
    @SerialName("partite") val events: List<Partita> = emptyList(),
    @SerialName("tags") val tags: List<Tag> = emptyList()
)