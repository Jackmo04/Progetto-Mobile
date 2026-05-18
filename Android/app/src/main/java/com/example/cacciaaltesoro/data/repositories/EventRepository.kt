@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.data.repositories

import android.location.Location
import android.util.Log
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.database.dto.TagDTO
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.User
import com.example.cacciaaltesoro.data.mappers.toDomain
import com.example.cacciaaltesoro.data.mappers.toDto
import com.example.cacciaaltesoro.data.mappers.toInsertDto
import com.example.cacciaaltesoro.utils.EventOrderType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.collections.plus
import kotlin.time.Clock

interface EventRepository {
    suspend fun insertEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun getEventWithTags(eventId: Int): Event?
    suspend fun setFoundTag(tag: Tag)
    suspend fun getEvent(id: Int): Event?
    suspend fun getAllEvents(): List<Event>
    suspend fun getEventsByCode(code: String): Event?
    suspend fun getOrderedEvent (type : String , location: Location?, listEvent: List<Event>) : List<Event>
    suspend fun getAllMyEvents(): List<Event>
    suspend fun getRegisteredAtEventNumber(idEvent: Int): Int?
    suspend fun joinToEvent(idEvent: Int )

    suspend fun unscribeFromEvent(idEvent: Int)

    suspend fun deleteEvent(idEvent: Int)

    suspend fun getTagCachedByMe(idEvent: Int): List<Tag>

}

@Serializable
data class SyncEventTags(
    @SerialName("p_eventid") val eventId: Int,
    @SerialName("p_tags") val tags: List<TagDTO>
)

@Serializable
data class UserEvent(
    @SerialName("prt_partita") val idEvent: Int,
    @SerialName("prt_utente") val idUser: String
)

@Serializable
data class UserTag(
    @SerialName("tgr_utente") val userId: String,
    @SerialName("tgr_tag") val tagId: String
)

class EventRepositoryImpl(private val supabase: SupabaseClient) : EventRepository {
    override suspend fun insertEvent(event: Event) {
        withContext(Dispatchers.IO) {
            val insertedEventId = supabase.from(SupabaseTables.EVENTS.tableName).upsert(event.toInsertDto()) {
                select()
            }.decodeSingle<EventDTO>().id

            val tagDTOs = event.tags.map { tag ->
                tag.copy(
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

    override suspend fun getEventWithTags(eventId: Int): Event? {
        return withContext(Dispatchers.IO) {
            val eventDto = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter { EventDTO::id eq eventId }
            }.decodeSingleOrNull<EventDTO>()

            val tagDTOs = supabase.from(SupabaseTables.TAGS.tableName).select {
                filter { TagDTO::eventId eq eventId }
            }.decodeList<TagDTO>()

            eventDto?.toDomain(tags = tagDTOs.map { it.toDomain() })
        }
    }

    override suspend fun setFoundTag(tag: Tag) {
        withContext(Dispatchers.IO) {
            val currentUser =
                supabase.auth.currentSessionOrNull()?.user?.id ?: throw IllegalStateException()

            supabase.from(SupabaseTables.TAG_CACHED.tableName).insert(
                UserTag(
                    userId = currentUser,
                    tagId = tag.id
                )
            )
        }
    }

    override suspend fun getEvent(id: Int): Event? {
        return try {
            Log.i("CardLog", id.toString() + "repo")
            val fetchedEvent = supabase.from(SupabaseTables.EVENTS.tableName).select(
                columns = Columns.raw("*, utenti!partite_par_organizzatore_fkey(*)")) {
                filter {
                    EventDTO::id eq id
                }
            }.decodeSingle<EventDTO>().toDomain()
            Log.i("CardLog", fetchedEvent.toString())
            fetchedEvent
        } catch (e: Exception) {
            Log.e("CardLog", "Error fetching event details for id: $id", e)
            null
        }
    }
    override suspend fun getEventsByCode(code: String): Event? {
        return try {
            supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::code eq code
                    EventDTO::organizerUUID neq supabase.auth.currentUserOrNull()?.id
                }
            }.decodeSingleOrNull<EventDTO>()?.toDomain()
        } catch (e: Exception) {
            Log.e("EventRepository", "Error fetching event", e)
            null
        }
    }

    override suspend fun getAllEvents(): List<Event> {
        val uuid = supabase.auth.currentUserOrNull()?.id
        val localTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        if (uuid == null){
            val listEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::isPrivate eq false
                    EventDTO::startTime gt localTime
                }
            }.decodeList<EventDTO>().sortedBy { eventDTO -> eventDTO.name }
            Log.i("Event", listEvent.toString())
            return  listEvent.map{e -> e.toDomain()}

        } else {
        try {
            val listEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::isPrivate eq false
                    EventDTO::organizerUUID neq uuid
                    EventDTO::startTime gt localTime
                }
            }.decodeList<EventDTO>().sortedBy { eventDTO -> eventDTO.name }
            Log.i("Event", listEvent.toString())
            return  listEvent.map{e -> e.toDomain()}

        } catch (e: Exception) {
            Log.e("EventRepository", "Error searching events", e)

        }}
        return emptyList()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getOrderedEvent(type: String , location: Location? , listEvent: List<Event>): List<Event> {
        var result = emptyList<Event>()
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

    override suspend fun getAllMyEvents(): List<Event> {


        try {
            val uuidC = supabase.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("utente non loggato")

            val userSaved = supabase.from(SupabaseTables.USERS.tableName).select(
                columns = Columns.raw("*,  partite!partecipazioni(*)")) {
                filter {
                    UserDTO::uuid eq uuidC
                }
            }.decodeSingle<UserDTO>().eventDTOS

            val createdEvent = supabase.from(SupabaseTables.EVENTS.tableName).select {
                filter {
                    EventDTO::organizerUUID eq uuidC
                }
            }.decodeList<EventDTO>()

            val listEvent = userSaved + createdEvent

            Log.d("SavedEventRepository", "Fetched events: ${listEvent.toString()}")
            return listEvent.distinct().map{e -> e.toDomain()}.sortedBy { eventDTO -> eventDTO.name }
        } catch (e: Exception) {
            Log.e("SavedEventRepository", "Error searching events", e)

        }
        return emptyList()
    }

    override suspend fun getRegisteredAtEventNumber(idEvent: Int): Int? {
        try {
            val result = supabase.from(SupabaseTables.SUBSCRIPTION.tableName).select {
                filter {
                    eq("prt_partita", idEvent)
                }
                count(Count.EXACT)
                head = true
            }.countOrNull()
            Log.i("CountR", result.toString())
            return result?.toInt()
        } catch (e: Exception) {
            Log.e("Count registered", e.toString())
        }
        return null
    }


    override suspend fun joinToEvent(idEvent: Int) {
        try {
            val link = UserEvent(idEvent,supabase.auth.currentSessionOrNull()?.user!!.id)
            supabase.postgrest[SupabaseTables.SUBSCRIPTION.tableName].insert(link)
        }catch (e: Exception){
            Log.e("JoinEvent",e.toString())
        }
    }

    override suspend fun unscribeFromEvent(idEvent: Int) {

        try {
            supabase.from(SupabaseTables.SUBSCRIPTION.tableName).delete {
                filter {
                    eq("prt_partita", idEvent)
                    eq("prt_utente", supabase.auth.currentSessionOrNull()?.user!!.id)
                }
            }
        }catch (e: Exception){
            Log.e("JoinEvent",e.toString())
        }
    }

    override suspend fun deleteEvent(idEvent: Int) {
        try {
            supabase.from(SupabaseTables.EVENTS.tableName).delete {
                filter {
                    eq("par_id", idEvent)
                }
            }
        }catch (e: Exception){
            Log.e("DeleteEvent",e.toString())
        }
    }

    override suspend fun getTagCachedByMe(idEvent: Int): List<Tag> {
        val uuidC = supabase.auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("utente non loggato")

        val  tags = supabase.from(SupabaseTables.USERS.tableName).select(
            columns = Columns.raw("*,  tags!tagraccolti(*)")) {
            filter {
                UserDTO::uuid eq uuidC
            }
        }.decodeSingle<UserDTO>().tagDTOS.filter { t -> t.eventId == idEvent }.map { t -> t.toDomain() }
        Log.i("tagc", tags.toString())
        return tags
    }

    private  fun orderLocationByDistance(
        eventList: List<Event>,
        location: Location?
    ): List<Event> {

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
