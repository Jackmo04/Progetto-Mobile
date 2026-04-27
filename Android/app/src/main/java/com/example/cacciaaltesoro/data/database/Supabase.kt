package com.example.cacciaaltesoro.data.database

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive

class Supabase {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://dkgltvsqbhtxfyywexlw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRrZ2x0dnNxYmh0eGZ5eXdleGx3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYyODk1NjcsImV4cCI6MjA5MTg2NTU2N30.HgZx1WiMAoz73OjAUjgjv3EnvH8juFdQ5CWmbN8V2hU"
    ) {
        install(Postgrest);
        install(Auth);
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
