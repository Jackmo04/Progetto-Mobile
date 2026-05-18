package com.example.cacciaaltesoro.ui.screens.onlineevents // Scegli un package unificato

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnlineEventsState(
    val listEvent: List<Event> = emptyList(),
    val uuid: String = "",
    val idEventCodeSearched: Int? = null,
    val currentFilter: EventFilterType = EventFilterType.ONLINE
)

data class OnlineEventsAction(
    val saveCurrentLocation: (Location) -> Unit,
    val saveIdEventCodeSearched: (String) -> Unit,
    val resetIdEventCodeSearched: () -> Unit,
    val loadEvents: (EventFilterType) -> Unit,
    val onOrderChanged: (String) -> Unit,
    val clearErrorMessage: () -> Unit
)

class OnlineEventsViewModel(
    private val repository: EventRepository,
    private val loginRepositoryImpl: LoginRepositoryImpl
) : ViewModel() {

    private var _state = MutableStateFlow(OnlineEventsState())
    var state = _state.asStateFlow()

    var currentLocation by mutableStateOf<Location?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set



    val action = OnlineEventsAction(
        saveCurrentLocation = { location -> currentLocation = location },

        loadEvents = { filterType ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state.update {
                        it.copy(
                            currentFilter = filterType,
                            uuid = loginRepositoryImpl.getLoggedUser()?.id ?: "",
                            listEvent = if (filterType == EventFilterType.ONLINE) {
                                repository.getAllEvents()
                            } else {
                                repository.getAllMyEvents()
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e("EventsViewModel", "Errore caricamento", e)
                    errorMessage = "Errore durante il caricamento degli eventi"
                } finally {
                    isLoading = false
                }
            }
        },

        saveIdEventCodeSearched = { code ->
            viewModelScope.launch {
                isLoading = true
                try {
                    val eventId = repository.getEventsByCode(code)?.id
                    _state.update { it.copy(idEventCodeSearched = eventId) }

                    if (eventId == null) {
                        errorMessage = "Evento non trovato"
                    }
                } catch (e: Exception) {
                    Log.e("EventsViewModel", "Errore ricerca", e)
                    errorMessage = "Errore durante la ricerca"
                } finally {
                    isLoading = false
                }
            }
        },

        resetIdEventCodeSearched = {
            _state.update { it.copy(idEventCodeSearched = null) }
        },

        onOrderChanged = { selected ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state.update {
                        it.copy(listEvent = repository.getOrderedEvent(selected, currentLocation, _state.value.listEvent))
                    }
                } catch (e: Exception) {
                    Log.e("EventsViewModel", "Errore ordinamento", e)
                } finally {
                    isLoading = false
                }
            }
        },

        clearErrorMessage = { errorMessage = null }
    )
    init {
        action.loadEvents(EventFilterType.ONLINE)
    }
}