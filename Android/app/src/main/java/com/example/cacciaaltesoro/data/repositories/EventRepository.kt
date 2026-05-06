package com.example.cacciaaltesoro.data.repositories

import android.location.Location
import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.toDto
import com.example.cacciaaltesoro.data.mappers.toInsertDto
import com.example.cacciaaltesoro.utils.EventOrderType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.plus
import kotlin.time.ExperimentalTime

interface EventRepository {
    suspend fun upsertEvent(event: Event)
    suspend fun getEvent(id: Int): EventDTO?
    suspend fun getAllEvents(query: String): List<EventDTO>
    suspend fun getEventsByCode(code: String): EventDTO?
    suspend fun getOrderedEvent (type : String , location: Location?) : List<EventDTO>
    suspend fun getAllMyEvents( uuid:String): List<EventDTO>
    suspend fun getOrderedMyEvent (type : String , location: Location?) : List<EventDTO>

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

    private var _event: EventDTO? = null
    val event: EventDTO?
        get() = _event

    private var _listEvent = listOf<EventDTO>()
    val listEvent: List<EventDTO>
        get() = _listEvent


    override suspend fun getEvent(id: Int): EventDTO? {
        return try {
            Log.i("CardLog", id.toString() + "repo")
            val fetchedEvent = supabase.from(SupabaseTables.EVENTS.tableName).select(
                columns = Columns.raw("*, utenti!partite_par_organizzatore_fkey(*)")) {
                filter {
                    EventDTO::id eq id
                }
            }.decodeSingle<EventDTO>()
            Log.i("CardLog", fetchedEvent.toString())
            _event = fetchedEvent
            fetchedEvent
        } catch (e: Exception) {
            Log.e("CardLog", "Error fetching event details for id: $id", e)
            null
        }
    }
    override suspend fun getEventsByCode(code: String): EventDTO? {
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::code eq code
                    EventDTO::organizerUUID neq supabase.auth.currentUserOrNull()?.id
                }
            }.decodeSingleOrNull<EventDTO>()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching event", e)
            null
        }
    }

    override suspend fun getAllEvents(query: String): List<EventDTO> {
        try {
            _listEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    ilike("par_nome", "%$query%")
                    EventDTO::isPrivate eq false
                    EventDTO::organizerUUID neq supabase.auth.currentUserOrNull()?.id
                }
            }.decodeList<EventDTO>().sortedBy { eventDTO -> eventDTO.name }
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
