package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Partita(
    @SerialName("par_id") val par_id: Int,
    @SerialName("par_nome") val par_nome: String,
    @SerialName("par_organizzatore") val par_organizzatore: String?,
    @SerialName("par_latitudine") val par_latitudine: Double,
    @SerialName("par_longitudine") val par_longitudine: Double,
    @SerialName("par_data")  val par_data: String,
    @SerialName("par_descrizione") val par_descrizione: String,
    @SerialName("par_codice") val par_codice: String,

    @SerialName("utenti")  val utente : Utente?

)