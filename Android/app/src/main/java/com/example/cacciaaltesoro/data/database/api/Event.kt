package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("partita")
data class Event(
    @SerialName("par_id") val id: Int,
    @SerialName("par_nome") val name: String,
    @SerialName("par_organizzatore") val organizerUUID: String?,
    @SerialName("par_latitudine") val lat: Double,
    @SerialName("par_longitudine") val lon: Double,
    @SerialName("par_data")  val dateTime: String,
    @SerialName("par_descrizione") val description: String,
    @SerialName("par_codice") val code: String,

    @SerialName("utenti")  val user : User? = null

)