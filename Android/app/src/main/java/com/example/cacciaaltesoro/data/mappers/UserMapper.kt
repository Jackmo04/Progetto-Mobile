package com.example.cacciaaltesoro.data.mappers

import com.example.cacciaaltesoro.data.database.dto.UserDTO
import com.example.cacciaaltesoro.data.domain.User

fun UserDTO.toDomain(): User {
    return User(
        uuid = uuid,
        username = username,
        image = image
    )
}