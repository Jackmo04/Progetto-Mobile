package com.example.cacciaaltesoro.ui.database.api

import kotlinx.serialization.Serializable

@Serializable
data class Partita(
    val par_id: Int,
    val par_nome: String,
    val par_organizzatore: Int,
    val par_latitudine: Double,
    val par_longitudine: Double,
    val par_data: String,
    val par_descrizione: String,
    val par_codice: String
)