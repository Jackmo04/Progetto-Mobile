package com.example.cacciaaltesoro.ui.database.api

import kotlinx.serialization.Serializable

@Serializable
data class Utente(
    val ute_id: Int,
    val ute_username: String,
    val ute_password: String,
    val ute_immagine: String

)