@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.newevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.example.cacciaaltesoro.data.repositories.EventRepository
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

data class NewEventState(
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
    val visibility: Visibility = Visibility.PUBLIC
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

data class NewEventActions(
    val onNameChange: (String) -> Unit,
    val onLocationChange: (Coordinates) -> Unit,
    val onStartDateChange: (year: Int, month: Int, day: Int) -> Unit,
    val onStartTimeChange: (hour: Int, minute: Int) -> Unit,
    val onEndDateChange: (year: Int, month: Int, day: Int) -> Unit,
    val onEndTimeChange: (hour: Int, minute: Int) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onVisibilityChange: (Visibility) -> Unit,
    val onSaveEvent: () -> Unit,
    val onCancelCreation: () -> Unit
)

class EventEditorViewModel(
    private val repository: EventRepository,
    private val eventId: Int? = null
) : ViewModel() {
    private val _state = MutableStateFlow(NewEventState())
    val state = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        eventId?.let { id ->
            _isEditMode.value = true
            viewModelScope.launch {
                _isLoading.value = true
                repository.getEventById(id)?.let { event ->
                    _state.update {
                        it.copy(
                            name = event.name,
                            location = Coordinates(event.lat, event.lon),
                            startDate = event.startTime.toJavaInstant().atZone(it.timeZone).toLocalDate(),
                            startTime = event.startTime.toJavaInstant().atZone(it.timeZone).toLocalTime(),
                            endDate = event.endTime.toJavaInstant().atZone(it.timeZone).toLocalDate(),
                            endTime = event.endTime.toJavaInstant().atZone(it.timeZone).toLocalTime(),
                            description = event.description ?: "",
                            visibility = if (event.isPrivate) Visibility.PRIVATE else Visibility.PUBLIC
                        )
                    }
                    checkTimestamps()
                }
                _isLoading.value = false
            }
        }
    }

    val actions = NewEventActions(
        onNameChange = { newName ->
            _state.update { it.copy(name = newName) }
        },
        onLocationChange = { newLocation ->
            _state.update { it.copy(location = newLocation) }
        },
        onStartDateChange = { year, month, day ->
            val localDate = LocalDate.of(year, month, day)
            if (localDate.isAfter(state.value.endDate)) {
                _state.update { it.copy(endDate = localDate) }
            }
            _state.update { it.copy(startDate = localDate) }
            checkTimestamps()
        },
        onStartTimeChange = { hour, minute ->
            val localTime = LocalTime.of(hour, minute)
            if (localTime.isAfter(state.value.endTime)) {
                _state.update { it.copy(endTime = localTime.plusHours(1)) }
            }
            _state.update { it.copy(startTime = localTime) }
            checkTimestamps()
        },
        onEndDateChange = { year, month, day ->
            val localDate = LocalDate.of(year, month, day)
            _state.update { it.copy(endDate = localDate) }
            checkTimestamps()
        },
        onEndTimeChange = { hour, minute ->
            val localTime = LocalTime.of(hour, minute)
            _state.update { it.copy(endTime = localTime) }
            checkTimestamps()
        },
        onDescriptionChange = { newDescription ->
            _state.update { it.copy(description = newDescription) }
        },
        onVisibilityChange = { visibility ->
            _state.update { it.copy(visibility = visibility) }
        },
        onSaveEvent = {
            _state.value.let {
                if (
                    it.name.isBlank() ||
                    it.location == null ||
                    it.isImpossibleEndDateTime ||
                    it.isImpossibleStartDateTime
                ) {
                    return@NewEventActions
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                _isLoading.value = true
                val event = state.value.let {
                    // TODO CHANGE TO ACTUAL DATA
                    Event(
                        id = eventId,
                        name = it.name,
                        organizerUUID = "5bbddf24-0be5-4348-bb0f-665c510307bf",
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
                        code = "PIPPO",
                        isPrivate = it.visibility == Visibility.PRIVATE,
                        tags = emptyList()
                    )
                }

                try {
                    if (!isEditMode.value) {
                        repository.insertEvent(event)
                    } else {
                        repository.updateEvent(event)
                    }
                    val message = if (isEditMode.value) "Evento aggiornato!" else "Evento creato!"
                    _uiEvent.send(message)
                    _state.update { NewEventState() }
                } catch (e: Exception) {
                    _uiEvent.send("Errore durante il salvataggio!")
                    e.printStackTrace()
                }
                _isLoading.value = false
            }
        },
        onCancelCreation = {
            // TODO add functionality to remove tags if needed
            _state.update { NewEventState() }
        }
    )

    // TODO improve this if I have time
    private fun checkTimestamps() {
        val now = LocalDateTime.now()
        val startDateTime = state.value.let { it.startDate.atTime(it.startTime) }
        val endDateTime = state.value.let { it.endDate.atTime(it.endTime) }

        val isStartPast = startDateTime.isBefore(now)
        val isEndBeforeStart = endDateTime.isBefore(startDateTime)
        val isEndPast = endDateTime.isBefore(now)

        _state.update {
            it.copy(
                isImpossibleStartDateTime = isStartPast,
                isImpossibleEndDateTime = isEndBeforeStart || isEndPast
            )
        }
    }
}