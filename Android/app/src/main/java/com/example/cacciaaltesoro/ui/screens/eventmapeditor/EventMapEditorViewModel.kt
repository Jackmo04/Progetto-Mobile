package com.example.cacciaaltesoro.ui.screens.eventmapeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.TagRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapEditorState(
    val markerPositions: List<LatLng>
)

data class MapEditorActions(
    val onAddMarker: (LatLng) -> Unit,
    val onRemoveMarker: (LatLng) -> Unit,
    val onSaveMarkers: (eventId: String) -> Unit
)

class EventMapEditorViewModel(private val repository: TagRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapEditorState(emptyList()))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(markerPositions = repository.getMarkers("test")) }
        }
    }

    val actions = MapEditorActions(
        onAddMarker = { latLng ->
            _state.update { it.copy(markerPositions = it.markerPositions + latLng) }
        },
        onRemoveMarker = { latLng ->
            _state.update { it.copy(markerPositions = it.markerPositions - latLng) }
        },
        onSaveMarkers = { eventId ->
            viewModelScope.launch {
                repository.saveMarkers(eventId, _state.value.markerPositions)
            }
        }
    )
}