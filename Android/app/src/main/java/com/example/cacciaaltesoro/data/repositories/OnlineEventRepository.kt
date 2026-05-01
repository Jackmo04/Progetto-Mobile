package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface OnlineEventRepository {
    suspend fun getAllEvents(query: String): List<EventDTO>
   // suspend fun searchEvents(query: String): Unit
}

class OnlineEventRepositoryImpl(private val supabase: SupabaseClient) : OnlineEventRepository {

  /*  override suspend fun searchEvents(): List<EventDTO> {
        var result = emptyList<Event>()
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select().decodeList<Event>()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching events", e)
            emptyList()
        }
    }*/

    override suspend fun getAllEvents(query: String): List<EventDTO> {
        var result = emptyList<Event>()
        try {
          result =  supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    ilike("par_nome", "%$query%")
                }
            }.decodeList<Event>()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)
            result = emptyList()
        }
        return result.stream().map { r -> r.toDto()}.toList()
    }
}
