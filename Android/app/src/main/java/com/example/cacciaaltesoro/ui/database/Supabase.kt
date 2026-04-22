package com.example.cacciaaltesoro.ui.database

import android.provider.ContactsContract
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.toJsonObject
import kotlinx.serialization.json.buildJsonObject

object Supabase {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://dkgltvsqbhtxfyywexlw.supabase.co",
        supabaseKey = "sb_publishable_G8rS-8Q792RWbbTQB3cMUQ_D7lUQHcY" // Ensure this is your correct 'anon' or 'service' key
    ) {
        install(Postgrest);
        install(Auth);
    }


    @OptIn(SupabaseInternal::class)
    suspend public fun signUp(email: String, pass: String, username: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = pass
            // Salviamo lo username nei metadati dell'utente Auth
            data = buildJsonObject {
                put("username", username.toJsonObject())
            }
        }
    }

    suspend public fun login(email: String, pass: String) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            println("Login effettuato!")
        } catch (e: Exception) {
            println("Errore: ${e.message}")
        }
    }
}
