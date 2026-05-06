@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.TagDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDomain
import com.example.cacciaaltesoro.data.mappers.toDto
import com.example.cacciaaltesoro.data.mappers.toInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime

interface EventRepository {
    suspend fun insertEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun getEventById(eventId: Int): Event?
}

@Serializable
data class SyncEventTags(
    @SerialName("p_eventid") val eventId: Int,
    @SerialName("p_tags") val tags: List<TagDTO>
)

class EventRepositoryImpl(private val supabase: SupabaseClient) : EventRepository {
    override suspend fun insertEvent(event: Event) {
        withContext(Dispatchers.IO) {
            val insertedEventId = supabase.from(SupabaseTables.EVENTS.tableName).upsert(event.toInsertDto()) {
                select()
            }.decodeSingle<EventDTO>().id

            val tagDTOs = event.tags.map { tag ->
                tag.copy(
                    id = UUID.randomUUID().toString(),
                    eventId = insertedEventId
                ).toDto()
            }

            if (tagDTOs.isNotEmpty()) {
                supabase.from(SupabaseTables.TAGS.tableName).insert(tagDTOs)
            }
        }
    }

    override suspend fun updateEvent(event: Event) {
        withContext(Dispatchers.IO) {
            val eventId = supabase.from(SupabaseTables.EVENTS.tableName).upsert(event.toDto()) {
                select()
            }.decodeSingle<EventDTO>().id

            val requestArgs = SyncEventTags(
                eventId = eventId,
                tags = event.tags.map { tag ->
                    tag.copy(eventId = eventId).toDto()
                }
            )

            supabase.postgrest.rpc(
                function = "sync_event_tags",
                parameters = requestArgs
            )
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
