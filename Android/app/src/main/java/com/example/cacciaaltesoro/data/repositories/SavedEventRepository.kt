package com.example.cacciaaltesoro.data.repositories

import android.location.Location
import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import com.example.cacciaaltesoro.utils.EventOrderType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlin.time.ExperimentalTime

interface SavedEventRepository {
    suspend fun getAllMyEvents( uuid:String): List<EventDTO>
    suspend fun getOrderedMyEvent (type : String , location: Location?) : List<EventDTO>
}


class SavedEventRepositoryImpl(private val supabase: SupabaseClient) : SavedEventRepository {

    private var _listEvent = listOf<EventDTO>()
    val listEvent: List<EventDTO>
        get() = _listEvent

    override suspend fun getAllMyEvents( uuid: String): List<EventDTO> {
        try {
            val userSaved = supabase.from(SupabaseTables.USERS.tableName).select(
                columns = Columns.raw("*, partite!partecipazioni(*)")) {
                filter {
                    UserDTO::uuid eq uuid
                }
            }.decodeSingle<UserDTO>().eventDTOS

            val createdEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::organizerUUID eq uuid
                }
            }.decodeList<EventDTO>()

            _listEvent = userSaved + createdEvent

            Log.d("SavedEventRepository", "Fetched events: ${_listEvent.size}")

        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)

        }
        return _listEvent.distinct().sortedBy { eventDTO -> eventDTO.name }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getOrderedMyEvent(type: String , location: Location?): List<EventDTO> {
        var result = emptyList<EventDTO>()
        try {
            when (type) {
                EventOrderType.NAME.type -> {
                    result = listEvent.sortedBy{ it.name }
                }
                EventOrderType.NAME_DESC.type -> {
                    result = listEvent.sortedByDescending{ it.name }
                }

                EventOrderType.START_DATE.type -> {
                    result = listEvent.sortedBy { it.startTime.epochSeconds }
                }

                EventOrderType.EVENT_DURATION.type -> {
                    result =  listEvent.sortedBy { it.endTime.nanosecondsOfSecond - it.startTime.nanosecondsOfSecond }
                }

                EventOrderType.DISTANCE.type ->{
                    result = try {
                        orderLocationByDistance(listEvent , location)
                    } catch (e: Exception){
                        listEvent
                    }
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


