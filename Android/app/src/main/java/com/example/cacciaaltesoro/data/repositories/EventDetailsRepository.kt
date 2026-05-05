package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface EventDetailsRepository {
    suspend fun getEvent(id: Int): EventDTO?
}


class EventDetailsRepositoryImpl(private val supabase: SupabaseClient) : EventDetailsRepository {

    private var _event: EventDTO? = null
    val event: EventDTO?
        get() = _event


    override suspend fun getEvent(id: Int): EventDTO? {
       return try {
           Log.i("CardLog", id.toString() + "repo")
           val fetchedEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::id eq id
                }
            }.decodeSingle<EventDTO>()
            Log.i("CardLog", fetchedEvent.toString())
           fetchedEvent
        } catch (e: Exception) {
            Log.e("CardLog", "Error searching events", e)
null
        }
    }


}


