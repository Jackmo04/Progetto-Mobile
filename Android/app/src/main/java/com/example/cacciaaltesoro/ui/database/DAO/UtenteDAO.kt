package com.example.cacciaaltesoro.ui.database.DAO

import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class UtenteDAO() {


    suspend public fun getUserByUsername( username: String): Utente?{
        return try {
            Supabase.supabase.from(TableName.UTENTI.tableName).select {
                filter {
                    Utente::ute_username eq username
                }
            }.decodeSingleOrNull<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend public fun getAllUsere(): List<Utente>?{
        return try {
            Supabase.supabase.from(TableName.UTENTI.tableName).select().decodeList<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}