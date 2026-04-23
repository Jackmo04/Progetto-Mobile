package com.example.cacciaaltesoro.ui.database.DAO

import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Partita
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class PartitaDAO() {


    suspend public fun getPartitaByID( id: Int): Partita?{
        return try {
            Supabase.supabase.from(TableName.PARTITE.tableName).select {
                filter {
                    Partita::par_id eq id
                }
            }.decodeSingleOrNull<Partita>()
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