package com.example.cacciaaltesoro.data.database

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive

class Supabase {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://dkgltvsqbhtxfyywexlw.supabase.co",
        supabaseKey = "sb_publishable_G8rS-8Q792RWbbTQB3cMUQ_D7lUQHcY"
    ) {
        install(Postgrest) {
            propertyConversionMethod = PropertyConversionMethod.SERIAL_NAME
        }
        install(Auth)
        install(Storage)
    }


    @OptIn(SupabaseInternal::class)
    suspend fun signUp(email: String, pass: String, username: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = pass

            data = buildJsonObject {
                put("username", JsonPrimitive(username))
            }
        }
    }

    suspend fun login(email: String, pass: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = pass
        }
    }
}
