package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface EventRepository {
    suspend fun insertEvent(event: Event)
}

class EventRepositoryImpl(private val supabase: SupabaseClient) : EventRepository {
    override suspend fun insertEvent(event: Event) {
        try {
            supabase.from(SupabaseTables.EVENTS.tableName).insert(event.toDto())
            Log.d("EventRepository", "Successfully saved new event: ${event.id}")
        } catch (e: Exception) {
            Log.e("EventRepository", "Error inserting event", e)
        }
    }
}
