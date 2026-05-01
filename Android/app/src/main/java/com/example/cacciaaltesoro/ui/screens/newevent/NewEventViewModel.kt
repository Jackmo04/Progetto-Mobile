@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.newevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class NewEventState(
    val name: String = "",
    val location: LatLng? = null,
    val startDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val timeZone: ZoneId = ZoneId.systemDefault(),
    val description: String = "",
) {
    val formattedDate: String
        get() {
            return startDate?.format(
                DateTimeFormatter
                    .ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
            ) ?: ""
        }

    val formattedTime: String
        get() {
            return startTime?.format(
                DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(Locale.getDefault())
            ) ?: ""
        }
}

data class NewEventActions(
    val onNameChange: (String) -> Unit,
    val onLocationChange: (LatLng) -> Unit,
    val onStartDateChange: (year: Int, month: Int, day: Int) -> Unit,
    val onStartTimeChange: (hour: Int, minute: Int) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSaveEvent: () -> Unit,
    val onCancelCreation: () -> Unit
)

class NewEventViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow(NewEventState())
    val state = _state.asStateFlow()

    val actions = NewEventActions(
        onNameChange = { newName ->
            _state.update { it.copy(name = newName) }
        },
        onLocationChange = { newLocation ->
            _state.update { it.copy(location = newLocation) }
        },
        onStartDateChange = { year, month, day ->
            val localDate = LocalDate.of(year, month, day)
            _state.update { it.copy(startDate = localDate) }
        },
        onStartTimeChange = { hour, minute ->
            val localTime = LocalTime.of(hour, minute)
            _state.update { it.copy(startTime = localTime) }
        },
        onDescriptionChange = { newDescription ->
            _state.update { it.copy(description = newDescription) }
        },
        onSaveEvent = {
            viewModelScope.launch {
                _state.value.let {
                    if (it.location == null || it.startDate == null || it.startTime == null) {
                        // TODO add ui change?
                        return@launch
                    }
                }
//                repository.insertEvent(Event()) TODO
                _state.update { NewEventState() }
            }
        },
        onCancelCreation = {
            // TODO add functionality to remove tags if needed
            _state.update { NewEventState() }
        }
    )
}