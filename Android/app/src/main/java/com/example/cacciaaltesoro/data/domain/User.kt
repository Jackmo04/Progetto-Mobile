package com.example.cacciaaltesoro.data.domain

import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.TagDTO
import kotlinx.serialization.SerialName

data class User(
    val uuid: String,
    val username: String,
    val image: String
)
