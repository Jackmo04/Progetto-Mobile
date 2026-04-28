package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class EventDAO(private val supabase: SupabaseClient) {

    suspend fun getEventById(id: Int): EventDTO? {
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select(columns = Columns.raw("*, utenti!partite_par_organizzatore_fkey(*)")) {
                filter {
                    EventDTO::id eq id
                }
            }.decodeSingleOrNull<EventDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}