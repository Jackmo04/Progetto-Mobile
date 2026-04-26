package com.example.cacciaaltesoro.ui.screens.newevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class NewEventState(
    val name: String = "",
    val location: LatLng? = null,
    val startDateTime: String = "",
    val description: String = "",

    val showMapModal: Boolean = false
)

data class NewEventActions(
    val onNameChange: (String) -> Unit,
    val onLocationChange: (LatLng) -> Unit,
    val onStartDateTimeChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSaveEvent: () -> Unit,
    val onCancelCreation: () -> Unit,
    val onSelectChooseLocation: () -> Unit,
    val onDismissChooseLocation: () -> Unit
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
        onStartDateTimeChange = { newDateTime ->
            _state.update { it.copy(startDateTime = newDateTime) }
        },
        onDescriptionChange = { newDescription ->
            _state.update { it.copy(description = newDescription) }
        },
        onSaveEvent = {
            viewModelScope.launch {
                if (_state.value.name.isBlank() || _state.value.location == null) {
                    return@launch
                }
                repository.insertEvent(
                    name = _state.value.name,
                    location = _state.value.location!!,
                    startDateTime = _state.value.startDateTime,
                    description = _state.value.description
                )
                _state.update { NewEventState() }
            }
        },
        onCancelCreation = {
            // TODO add functionality to remove tags if needed
            _state.update { NewEventState() }
        },
        onSelectChooseLocation = {
            _state.update { it.copy(showMapModal = true) }
        },
        onDismissChooseLocation = {
            _state.update { it.copy(showMapModal = false) }
        }
    )
}