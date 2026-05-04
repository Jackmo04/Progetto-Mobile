package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface OnlineEventRepository {
    suspend fun getAllEvents(query: String): List<EventDTO>
   // suspend fun searchEvents(query: String): Unit

    suspend fun getOrderedEvent (type : String) : List<EventDTO>
}


class OnlineEventRepositoryImpl(private val supabase: SupabaseClient) : OnlineEventRepository {

  /*  override suspend fun searchEvents(): List<EventDTO> {
        var result = emptyList<EventDTO>()
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select().decodeList<EventDTO>()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching events", e)
            emptyList()
        }
    }*/

    override suspend fun getAllEvents(query: String): List<EventDTO> {
        return try {
            val result = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    ilike("par_nome", "%$query%")
                }
            }.decodeList<EventDTO>()
            Log.i("Event", result.toString())
            result
        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)
            emptyList()
        }
    }

    override suspend fun getOrderedEvent(type: String): List<EventDTO> {
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select().decodeList<EventDTO>()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching ordered events", e)
            emptyList()
        }
    }
}
