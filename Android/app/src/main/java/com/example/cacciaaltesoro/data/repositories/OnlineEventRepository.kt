package com.example.cacciaaltesoro.data.repositories

import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.utils.EventOrderType
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlin.time.ExperimentalTime

interface OnlineEventRepository {
    suspend fun getAllEvents(query: String): List<EventDTO>
   // suspend fun searchEvents(query: String): Unit

    suspend fun getOrderedEvent (type : String , location: Location?) : List<EventDTO>
}


class OnlineEventRepositoryImpl(private val supabase: SupabaseClient) : OnlineEventRepository {

    private var _listEvent = listOf<EventDTO>()
    val listEvent: List<EventDTO>
        get() = _listEvent

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
        try {
            _listEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    ilike("par_nome", "%$query%")
                }
            }.decodeList<EventDTO>()
            Log.i("Event", _listEvent.toString())

        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)

        }
        return _listEvent
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getOrderedEvent(type: String , location: Location?): List<EventDTO> {
        var result = emptyList<EventDTO>()
        try {
            when (type) {
                EventOrderType.NAME.type -> {
                    result = listEvent.sortedByDescending{ it.name }
                }

                EventOrderType.START_DATE.type -> {
                    result = listEvent.sortedBy { it.startTime.epochSeconds }
                }

                EventOrderType.EVENT_DURATION.type -> {
                    result =  listEvent.sortedBy { it.endTime.nanosecondsOfSecond - it.startTime.nanosecondsOfSecond }
                }

                EventOrderType.DISTANCE.type -> {
                    result = orderLocationByDistance(listEvent , location) // Distance sorting usually requires user location context
                }
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching ordered events", e)
        }
        return result
    }

    private  fun orderLocationByDistance(
        eventList: List<EventDTO>,
        location: Location?
    ): List<EventDTO> {

        return eventList.sortedBy { place ->
            val eventPosition = Location("provider_temporaneo").apply {
                latitude = place.lat
                longitude = place.lon
            }
            Log.i("Location" , location.toString())

            location?.distanceTo(eventPosition)
        }
    }
}


