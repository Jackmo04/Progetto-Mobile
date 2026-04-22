package com.example.cacciaaltesoro.ui.database.DAO

import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.postgrest.from

class UtenteDAO {

    val conn = Supabase.supabase

    suspend public fun getUserByUsername( username: String): Utente?{

        return try {
            conn.from(TableName.UTENTI.tableName).select {
                filter {
                    Utente::ute_username eq username
                }
            }.decodeSingleOrNull<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}