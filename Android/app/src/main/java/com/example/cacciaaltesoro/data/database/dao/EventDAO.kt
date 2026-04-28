package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.api.Event
import com.example.cacciaaltesoro.data.database.api.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class EventDAO(private val supabase: SupabaseClient) {

    suspend fun getEventById(id: Int): Event? {
        return try {
            supabase.from(TableName.PARTITE.tableName).select(columns = Columns.raw("*, utenti!partite_par_organizzatore_fkey(*)")) {
                filter {
                    Event::id eq id
                }
            }.decodeSingleOrNull<Event>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUsers(): List<User>? {
        return try {
            supabase.from(TableName.UTENTI.tableName).select().decodeList<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}