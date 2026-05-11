package com.example.cacciaaltesoro.ui.screens.savedevents

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    private val repository: EventRepository,
    private val loginRepositoryImpl: LoginRepositoryImpl
) : ViewModel() {

    private var _state = MutableStateFlow(SavedEventState())

    var state = _state.asStateFlow()

    var currentLocation by mutableStateOf<Location?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {

            try {
                isLoading = true
                _state.update {
                    it.copy(listEvent = repository.getAllMyEvents( loginRepositoryImpl.userId.first()),
                            uuid = loginRepositoryImpl.userId.first())
                }
            }finally {
                isLoading = false
            }


        }
    }

    @OptIn(ExperimentalTime::class)
    val action = SavedEventAction(
        saveCurrentLocation = {location -> currentLocation = location},
        onSearchEvent = {
            viewModelScope.launch {
                isLoading = true
                _state.update {
                    it.copy(uuid = loginRepositoryImpl.userId.first())
                }
                try {
                    _state.update {
                        it.copy(listEvent = repository.getAllMyEvents(loginRepositoryImpl.userId.first()))
                    }

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
                _state.update {
                    it.copy(listEvent = repository.getOrderedMyEvent(selected, currentLocation))
                }
            } finally {
                isLoading = false
            }
        }
        }
    )
}
