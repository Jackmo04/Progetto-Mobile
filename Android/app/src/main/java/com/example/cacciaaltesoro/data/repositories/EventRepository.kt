package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDto
import com.example.cacciaaltesoro.data.mappers.toInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface EventRepository {
    suspend fun upsertEvent(event: Event)
}

class EventRepositoryImpl(private val supabase: SupabaseClient) : EventRepository {
    override suspend fun upsertEvent(event: Event) {
        withContext(Dispatchers.IO) {
            val insertedEventId = supabase.from(SupabaseTables.EVENTS.tableName).upsert(event.toInsertDto()) {
                select()
            }.decodeSingle<EventDTO>().id

            val tagDTOs = event.tags.map { tag ->
                tag.copy(eventId = insertedEventId).toInsertDto()
            }

            if (tagDTOs.isNotEmpty()) {
                supabase.from(SupabaseTables.TAGS.tableName).upsert(tagDTOs)
            }
        }
    }
}
