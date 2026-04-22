package com.example.cacciaaltesoro.ui.database.api

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val tag_id: Int,
    val tag_posizione: Int,
    val tag_partita: Int,
    val tag_hash: String,
    val tag_latitudine: Double,
    val tag_longitudine: Double,
    val tag_indizio: String,
    val tag_immagine: String
)