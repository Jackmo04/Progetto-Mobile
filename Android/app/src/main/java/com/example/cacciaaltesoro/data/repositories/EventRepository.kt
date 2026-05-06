@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.TagDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDomain
import com.example.cacciaaltesoro.data.mappers.toInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

interface EventRepository {
    suspend fun upsertEvent(event: Event)
    suspend fun getEventById(eventId: Int): Event?
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

    override suspend fun getEventById(eventId: Int): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val eventDto = supabase.from(SupabaseTables.EVENTS.tableName).select {
                    filter { EventDTO::id eq eventId }
                }.decodeSingleOrNull<EventDTO>()

                val tagDTOs = supabase.from(SupabaseTables.TAGS.tableName).select {
                    filter { TagDTO::eventId eq eventId }
                }.decodeList<TagDTO>()

                eventDto?.toDomain(tags = tagDTOs.map { it.toDomain() })
            } catch (e: Exception) {
                Log.e("EventRepository", "Error fetching event $eventId", e)
                null
            }
        }
    }
}
