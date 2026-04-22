package com.example.cacciaaltesoro.ui.database

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object Supabase {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://dkgltvsqbhtxfyywexlw.supabase.co",
        supabaseKey = "sb_publishable_G8rS-8Q792RWbbTQB3cMUQ_D7lUQHcY" // Ensure this is your correct 'anon' or 'service' key
    ) {
        install(Postgrest)
    }
}
