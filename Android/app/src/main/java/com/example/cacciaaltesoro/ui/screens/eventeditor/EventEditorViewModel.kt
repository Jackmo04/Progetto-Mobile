@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.eventeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.Locale
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

enum class Visibility(val labelRes: Int) {
    PUBLIC(R.string.public_k),
    PRIVATE(R.string.private_k)
}

data class EventState(
    val name: String = "",
    val location: Coordinates? = null,
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().plusHours(1),
    val endDate: LocalDate = startDate,
    val endTime: LocalTime = startTime.plusHours(1),
    val isImpossibleStartDateTime: Boolean = false,
    val isImpossibleEndDateTime: Boolean = false,
    val timeZone: ZoneId = ZoneId.systemDefault(),
    val description: String = "",
    val visibility: Visibility = Visibility.PUBLIC,
    val code: String = "",
    val tags: List<Tag> = emptyList()
) {
    val fStartDate: String get() = startDate.formatShortDate()
    val fStartTime: String get() = startTime.formatShortTime()
    val fEndDate: String get() = endDate.formatShortDate()
    val fEndTime: String get() = endTime.formatShortTime()

    private fun LocalDate.formatShortDate(): String = formatWithStyle(FormatStyle.SHORT, isDate = true)
    private fun LocalTime.formatShortTime(): String = formatWithStyle(FormatStyle.SHORT, isDate = false)

    private fun TemporalAccessor.formatWithStyle(style: FormatStyle, isDate: Boolean): String {
        val formatter = if (isDate) DateTimeFormatter.ofLocalizedDate(style) else DateTimeFormatter.ofLocalizedTime(style)
        return formatter.withLocale(Locale.getDefault()).format(this)
    }
}

data class UIState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false
)

data class EventActions(
    val onNameChange: (String) -> Unit,
    val onLocationChange: (Coordinates) -> Unit,
    val onStartDateChange: (year: Int, month: Int, day: Int) -> Unit,
    val onStartTimeChange: (hour: Int, minute: Int) -> Unit,
    val onEndDateChange: (year: Int, month: Int, day: Int) -> Unit,
    val onEndTimeChange: (hour: Int, minute: Int) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onVisibilityChange: (Visibility) -> Unit,
    val onSaveEvent: () -> Unit,
    val onCancelCreation: () -> Unit,
    val onEditTagsClick: () -> Boolean
)

data class TagActions(
    val onNewTag: (Coordinates) -> Tag,
    val onDeleteTag: (Tag) -> Unit,
    val onUpdateTag: (Tag) -> Unit
)

class EventEditorViewModel(
    private val eventRepository: EventRepository,
    private val loginRepository: LoginRepositoryImpl,
    private val eventId: Int? = null
) : ViewModel() {
    private val _eventState = MutableStateFlow(EventState())
    val eventState = _eventState.asStateFlow()

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        eventId?.let { id ->
            _uiState.update { it.copy(isEditMode = true) }
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                try {
                    eventRepository.getEventWithTags(id)?.let { event ->
                        _eventState.update {
                            it.copy(
                                name = event.name,
                                location = Coordinates(event.lat, event.lon),
                                startDate = event.startTime.toJavaInstant().atZone(it.timeZone).toLocalDate(),
                                startTime = event.startTime.toJavaInstant().atZone(it.timeZone).toLocalTime(),
                                endDate = event.endTime.toJavaInstant().atZone(it.timeZone).toLocalDate(),
                                endTime = event.endTime.toJavaInstant().atZone(it.timeZone).toLocalTime(),
                                description = event.description ?: "",
                                visibility = if (event.isPrivate) Visibility.PRIVATE else Visibility.PUBLIC,
                                code = event.code,
                                tags = event.tags
                            )
                        }
                        checkTimestamps()
                    }
                } catch (e: Exception) {
                    _uiEvent.send("Errore durante il caricamento dell'evento!") // TODO change to res
                    e.printStackTrace()
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    val eventActions = EventActions(
        onNameChange = { newName ->
            _eventState.update { it.copy(name = newName) }
        },
        onLocationChange = { newLocation ->
            _eventState.update { it.copy(location = newLocation) }
        },
        onStartDateChange = { year, month, day ->
            val localDate = LocalDate.of(year, month, day)
            if (localDate.isAfter(eventState.value.endDate)) {
                _eventState.update { it.copy(endDate = localDate) }
            }
            _eventState.update { it.copy(startDate = localDate) }
            checkTimestamps()
        },
        onStartTimeChange = { hour, minute ->
            val localTime = LocalTime.of(hour, minute)
            if (localTime.isAfter(eventState.value.endTime)) {
                _eventState.update { it.copy(endTime = localTime.plusHours(1)) }
            }
            _eventState.update { it.copy(startTime = localTime) }
            checkTimestamps()
        },
        onEndDateChange = { year, month, day ->
            val localDate = LocalDate.of(year, month, day)
            _eventState.update { it.copy(endDate = localDate) }
            checkTimestamps()
        },
        onEndTimeChange = { hour, minute ->
            val localTime = LocalTime.of(hour, minute)
            _eventState.update { it.copy(endTime = localTime) }
            checkTimestamps()
        },
        onDescriptionChange = { newDescription ->
            _eventState.update { it.copy(description = newDescription) }
        },
        onVisibilityChange = { visibility ->
            _eventState.update { it.copy(visibility = visibility) }
        },
        onSaveEvent = {
            _eventState.value.let {
                if (
                    it.name.isBlank() ||
                    it.location == null ||
                    it.isImpossibleEndDateTime ||
                    it.isImpossibleStartDateTime
                ) {
                    return@EventActions
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                val user = loginRepository.getLoggedUser()
                if (user == null) {
                    _uiEvent.send("Errore! Login non effettuato!") // TODO change this
                    return@launch
                }
                _uiState.update { it.copy(isLoading = true) }
                val event = eventState.value.let {
                    Event(
                        id = eventId,
                        name = it.name,
                        organizerUUID = user.id,
                        lat = it.location!!.latitude,
                        lon = it.location.longitude,
                        startTime = it.startDate
                            .atTime(it.startTime)
                            .atZone(it.timeZone)
                            .toInstant()
                            .toKotlinInstant(),
                        endTime = it.endDate
                            .atTime(it.endTime)
                            .atZone(it.timeZone)
                            .toInstant()
                            .toKotlinInstant(),
                        description = it.description,
                        code = if (uiState.value.isEditMode) it.code else UUID.randomUUID().toString().take(8).uppercase(),
                        isPrivate = it.visibility == Visibility.PRIVATE,
                        tags = it.tags
                    )
                }

                try {
                    if (!uiState.value.isEditMode) {
                        eventRepository.insertEvent(event)
                    } else {
                        eventRepository.updateEvent(event)
                    }
                    val message = if (uiState.value.isEditMode) "Evento aggiornato!" else "Evento creato!"
                    _uiEvent.send(message)
                    _eventState.update { EventState() }
                } catch (e: Exception) {
                    _uiEvent.send("Errore durante il salvataggio!")
                    e.printStackTrace()
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        },
        onCancelCreation = {
            // TODO add functionality to remove tags if needed
            _eventState.update { EventState() }
        },
        onEditTagsClick = {
            if (eventState.value.location == null) {
                viewModelScope.launch { _uiEvent.trySend("Prima seleziona un punto di ritrovo!") }
            }
            eventState.value.location != null
        }
    )

    val tagActions = TagActions(
        onNewTag = { coordinates ->
            val newTag = Tag(
                id = UUID.randomUUID().toString(),
                number = eventState.value.tags.size + 1,
                coordinates = coordinates,
                eventId = eventId
            )
            _eventState.update {
                it.copy(tags = it.tags + newTag)
            }
            newTag
        },
        onDeleteTag = { tagToDelete ->
            _eventState.update { state ->
                val newTags = (state.tags - tagToDelete).mapIndexed { index, tag ->
                    tag.copy(number = index + 1)
                }
                state.copy(tags = newTags)
            }
        },
        onUpdateTag = { tag ->
            _eventState.update { state ->
                state.copy(
                    tags = state.tags.map { if (it.id == tag.id) tag else it }
                )
            }
        }
    )

    // TODO improve this if I have time
    private fun checkTimestamps() {
        val now = LocalDateTime.now()
        val startDateTime = eventState.value.let { it.startDate.atTime(it.startTime) }
        val endDateTime = eventState.value.let { it.endDate.atTime(it.endTime) }

        val isStartPast = startDateTime.isBefore(now)
        val isEndBeforeStart = endDateTime.isBefore(startDateTime)
        val isEndPast = endDateTime.isBefore(now)

        _eventState.update {
            it.copy(
                isImpossibleStartDateTime = isStartPast,
                isImpossibleEndDateTime = isEndBeforeStart || isEndPast
            )
        }
    }
}