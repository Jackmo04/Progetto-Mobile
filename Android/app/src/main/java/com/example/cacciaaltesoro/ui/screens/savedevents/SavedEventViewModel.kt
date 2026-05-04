package com.example.cacciaaltesoro.ui.screens.savedevents

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import com.example.cacciaaltesoro.data.repositories.SavedEventRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class SavedEventState(
    val listEvent: List<EventDTO> = emptyList(),
    val uuid: String = ""
)

data class SavedEventAction(
    val saveCurrentLocation: (Location) -> Unit,
    val onSearchEvent: (String) -> Unit,
    val onViewEvent: (String) -> Unit,
    val onOrderChanged: (String) -> Unit
)

class SavedEventsViewModel(
    private val repository: SavedEventRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private var _state by mutableStateOf(SavedEventState())
    fun getState() = _state

    var currentLocation by mutableStateOf<Location?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {

            try {
                isLoading = true
                _state = _state.copy(listEvent = repository.getAllMyEvents( loginRepository.userId.first()))
                _state = _state.copy(uuid = loginRepository.userId.first())
            }finally {
                isLoading = false
            }


        }
    }

    @OptIn(ExperimentalTime::class)
    val action = SavedEventAction(
        saveCurrentLocation = {location -> currentLocation = location},
        onSearchEvent = { query ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state = _state.copy(listEvent = repository.getAllMyEvents(loginRepository.userId.first()))

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
                _state =
                    _state.copy(listEvent = repository.getOrderedMyEvent(selected, currentLocation))
            } finally {
                isLoading = false
            }
        }
        }
    )
}
