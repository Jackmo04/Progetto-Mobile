package com.example.cacciaaltesoro.ui.database.api

import kotlinx.serialization.Serializable

@Serializable
data class Utente(
    val ute_id: Int? = null,
    val ute_username: String? = null,
    val ute_password: String? = null,
    val ute_immagine: String? = null
)
