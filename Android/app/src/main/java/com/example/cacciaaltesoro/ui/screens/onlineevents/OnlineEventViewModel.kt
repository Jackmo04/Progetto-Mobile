package com.example.cacciaaltesoro.ui.screens.onlineevents

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import com.example.cacciaaltesoro.utils.EventOrderType
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class OnlineEventState(
    val ListEvent: List<EventDTO> = emptyList()
)

data class OnlineEventAction(
    val saveCurrentLocation: (Location) -> Unit,
    val onSearchEvent: (String) -> Unit,
    val onViewEvent: (String) -> Unit,
    val onOrderChanged: (String) -> Unit
)

class OnlineEventViewModel(
    private val repository: OnlineEventRepository
) : ViewModel() {

    private var _state by mutableStateOf(OnlineEventState())
    fun getState() = _state

    var currentLocation by mutableStateOf<Location?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {

            try {
                isLoading = true
                _state = _state.copy(ListEvent = repository.getAllEvents("%"))
            }finally {
                isLoading = false
            }


        }
    }

    @OptIn(ExperimentalTime::class)
    val action = OnlineEventAction(
        saveCurrentLocation = {location -> currentLocation = location},
        onSearchEvent = { query ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state = _state.copy(ListEvent = repository.getAllEvents(query))
                } catch (e: Exception) {
                    errorMessage = "Errore durante la ricerca"
                } finally {
                    isLoading = false
                }
            }
        },
        onViewEvent = {

        },
        onOrderChanged = { selected ->
            viewModelScope.launch {
            isLoading = true
            try {
                _state = _state.copy(ListEvent = repository.getOrderedEvent(selected))
            } catch (e: Exception) {
                errorMessage = "Errore durante la ricerca"
            } finally {
                isLoading = false
            }
        }
        }
    )
}
