package com.example.cacciaaltesoro.ui.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    @SerialName("tag_id")  val tag_id: Int,
    @SerialName("tag_posizione")  val tag_posizione: Int,
    @SerialName("tag_partita") val tag_partita: Int,
    @SerialName("tag_hash") val tag_hash: String,
    @SerialName("tag_latitudine") val tag_latitudine: Double,
    @SerialName("tag_longitudine") val tag_longitudine: Double,
    @SerialName("tag_indizio") val tag_indizio: String,
    @SerialName("tag_immagine") val tag_immagine: String
)