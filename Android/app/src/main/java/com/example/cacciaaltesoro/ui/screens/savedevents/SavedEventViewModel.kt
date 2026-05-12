package com.example.cacciaaltesoro.ui.screens.savedevents

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class SavedEventState(
    val listEvent: List<Event> = emptyList(),
    val uuid: String = ""
)

data class SavedEventAction(
    val saveCurrentLocation: (Location) -> Unit,
    val onSearchEvent: () -> Unit,
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
                    it.copy(listEvent = repository.getAllMyEvents())
                }
                _state.update {
                    it.copy(uuid = loginRepositoryImpl.getLoggedUser()!!.id)
                }
                Log.i("myevent", _state.value.listEvent.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
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
                        it.copy(listEvent = repository.getAllMyEvents())
                    }

                } finally {
                    isLoading = false
                }
            }
        },
        onOrderChanged = { selected ->
            viewModelScope.launch {
            isLoading = true
            try {
                _state.update {
                    it.copy(listEvent = repository.getOrderedEvent(selected, currentLocation , _state.value.listEvent))
                }
            } finally {
                isLoading = false
            }
        }
        }
    )
}
