package com.example.cacciaaltesoro.data.repositories

import android.location.Location
import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.utils.EventOrderType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlin.time.ExperimentalTime

interface EventDetailsRepository {
    suspend fun getEvents(id: Int): EventDTO?
}


class EventDetailsRepositoryImpl(private val supabase: SupabaseClient) : EventDetailsRepository {

    private var _event: EventDTO? = null
    val event: EventDTO?
        get() = _event


    override suspend fun getEvents(id: Int): EventDTO? {
        try {
            _event = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::id eq id
                }
            }.decodeSingle<EventDTO>()
            Log.i("Event", _event.toString())

        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)

        }
        return _event
    }


}


