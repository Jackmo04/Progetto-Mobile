package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Utente(
    @SerialName("ute_id") val ute_id: String? = null,
    @SerialName("ute_username") val ute_username: String? = null,
    @SerialName("ute_password") val ute_password: String? = null,
    @SerialName("ute_immagine") val ute_immagine: String? = null,
    @SerialName("partite") val partite: List<Partita> = emptyList(),
    @SerialName("tags")val tags: List<Tag> = emptyList()
)