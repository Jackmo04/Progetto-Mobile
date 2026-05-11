package com.example.cacciaaltesoro.ui.screens.onlineevents

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

data class OnlineEventState(
    val listEvent: List<EventDTO> = emptyList(),
    val uuid: String = "",
    val idEventCodeSearched: Int? = null
)

data class OnlineEventAction(
    val saveCurrentLocation: (Location) -> Unit,
    val saveIdEventCodeSearched:(String) -> Unit,
    val resetIdEventCodeSearched: () -> Unit,
    val onSearchEvent: (String) -> Unit,
    val onOrderChanged: (String) -> Unit
)

class OnlineEventViewModel(
    private val repository: EventRepository,
    private val loginRepositoryImpl: LoginRepositoryImpl
) : ViewModel() {

    private var _state = MutableStateFlow(OnlineEventState())
    var state = _state.asStateFlow()
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
                _state.update {
                    it.copy(listEvent = repository.getAllEvents("%"),
                        uuid = loginRepositoryImpl.userId.first())
                }
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
                    _state.update {
                        it.copy(listEvent = repository.getAllEvents(query))
                    }
                } catch (e: Exception) {
                    errorMessage = "Errore durante la ricerca"
                } finally {
                    isLoading = false
                }
            }
        },
        saveIdEventCodeSearched = {code ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state.update {
                        it.copy(
                            idEventCodeSearched = repository.getEventsByCode(code)?.id
                        )
                    }
                } catch (e: Exception) {
                    errorMessage = "Errore durante la ricerca"
                } finally {
                    isLoading = false
                }
            }

        },
        resetIdEventCodeSearched={
            viewModelScope.launch {
            _state.update {
                it.copy(idEventCodeSearched = null)
            }
            }
        }
        ,
        onOrderChanged = { selected ->
            viewModelScope.launch {
            isLoading = true
            try {
                _state.update {
                    it.copy(listEvent = repository.getOrderedEvent(selected , currentLocation))
                }
            } catch (e: Exception) {
                errorMessage = "Errore durante la ricerca"
            } finally {
                isLoading = false
            }
        }
        }
    )
}
