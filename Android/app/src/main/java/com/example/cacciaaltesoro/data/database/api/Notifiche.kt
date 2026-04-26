package com.example.cacciaaltesoro.data.database.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notifiche(
    @SerialName("not_id") val not_id: Int? = null,
    @SerialName("not_utente") val not_utente: String? = null,
    @SerialName("not_data") val not_data: String? = null,
    @SerialName("not_messaggio") val not_messaggio: String? = null,
    @SerialName("not_letto") val not_letto: Boolean? = null,
    @SerialName("utenti")  val utente : Utente?
)
