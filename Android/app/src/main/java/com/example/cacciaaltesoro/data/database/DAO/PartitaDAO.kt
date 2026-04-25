package com.example.cacciaaltesoro.data.database.DAO

import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.database.api.Partita
import com.example.cacciaaltesoro.data.database.api.Utente
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class PartitaDAO() {


    suspend public fun getPartitaByID( id: Int): Partita?{
        return try {
            Supabase.supabase.from(TableName.PARTITE.tableName).select(columns = Columns.raw("*, utenti!partite_par_organizzatore_fkey(*)")) {
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