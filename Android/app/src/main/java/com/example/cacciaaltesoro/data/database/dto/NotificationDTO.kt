package com.example.cacciaaltesoro.data.database.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("notifiche")
data class NotificationDTO(
    @SerialName("not_id") val id: Int? = null,
    @SerialName("not_utente") val userUUID: String? = null,
    @SerialName("not_data") val dateTime: String? = null,
    @SerialName("not_messaggio") val message: String? = null,
    @SerialName("not_letto") val isRead: Boolean? = null,
    @SerialName("utenti")  val userDTO : UserDTO?
)
