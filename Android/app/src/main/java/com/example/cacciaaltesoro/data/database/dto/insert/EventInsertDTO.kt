package com.example.cacciaaltesoro.data.database.dto.insert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
@SerialName("partita")
data class EventInsertDTO(
    @SerialName("par_nome") val name: String,
    @SerialName("par_organizzatore") val organizerUUID: String,
    @SerialName("par_latitudine") val lat: Double,
    @SerialName("par_longitudine") val lon: Double,
    @SerialName("par_timestamp_inizio") val startTime: Instant,
    @SerialName("par_timestamp_fine") val endTime: Instant,
    @SerialName("par_descrizione") val description: String?,
    @SerialName("par_codice") val code: String,
    @SerialName("par_privato") val isPrivate: Boolean
)