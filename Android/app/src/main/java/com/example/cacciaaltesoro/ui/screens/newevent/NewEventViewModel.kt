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
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.Locale
import kotlin.time.ExperimentalTime
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
    val visibility: Visibility = Visibility.PUBLIC,
    val isLoading: Boolean = false
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

class NewEventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow(NewEventState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

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
            viewModelScope.launch(Dispatchers.IO) {
                _state.update { it.copy(isLoading = true) }
                val event = state.value.let {
                    // TODO CHANGE TO ACTUAL DATA
                    Event(
                        id = null,
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
                        tags = setOf(
                            Tag(
                                number = 1,
                                coordinates = Coordinates(15.0, 20.0),
                                textHint = null,
                                imageHint = null
                            ),
                            Tag(
                                number = 2,
                                coordinates = Coordinates(6.0, 7.0),
                                textHint = null,
                                imageHint = null
                            ),
                            Tag(
                                number = 3,
                                coordinates = Coordinates(40.0, 75.0),
                                textHint = null,
                                imageHint = null
                            )
                        )
                    )
                }

                try {
                    repository.upsertEvent(event)
                    _uiEvent.send("Evento creato con successo!") // TODO
                } catch (e: Exception) {
                    _uiEvent.send("Errore durante la creazione evento!")
                }

                _state.update { NewEventState() }
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