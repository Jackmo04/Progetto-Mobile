package com.example.cacciaaltesoro.ui.screens.eventdetails

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.User
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class EventDetailsEventState(
    val idEvent: Int = 0,
    val event: EventDTO? = null,
    val userId: String? = null,
    val imSubscribe: Boolean = false,
    val isLoadingSubscription: Boolean = true
)

data class EventDetailsEventAction(
    val findEventByID: (Int) -> Unit,
    val saveIdUser: () -> Unit,
    val joinToEvent:() -> Unit,
    val unscribeFromEvent:() -> Unit,
    val deleteEvent:() -> Unit
)

class EventDetailsViewModel(
    private val repository: EventRepository,
    private val loginRepositoryImpl: LoginRepositoryImpl
) : ViewModel() {

    private var _state = MutableStateFlow(EventDetailsEventState())

    val state = _state.asStateFlow()

    @OptIn(ExperimentalTime::class)
    val action = EventDetailsEventAction(
        findEventByID = { id ->
            viewModelScope.launch {
                try {
                    _state.update {
                        it.copy(
                            idEvent = id,
                            event = repository.getEvent(id)
                        )
                    }
                } catch (e: Exception){
                    Log.e("EventDetailsViewModel", "Errore nel caricamento evento", e)
                }
            }
        },
        saveIdUser = {
            viewModelScope.launch {
                loginRepositoryImpl.userId.collect { userId ->
                    _state.update { it.copy(userId = userId, isLoadingSubscription = true) }
                    try {
                        val myEvents = repository.getAllMyEvents()
                        val isSubscribed = myEvents.any { it.id == _state.value.idEvent }
                        _state.update {
                            it.copy(
                                imSubscribe = isSubscribed,
                                isLoadingSubscription = false
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("EventDetailsViewModel", "Error checking subscription: ${e.message}")
                        _state.update { it.copy(isLoadingSubscription = false) }
                    }
                }
            }
        },
        joinToEvent = {
            viewModelScope.launch {
                _state.update { it.copy(isLoadingSubscription = true) } // Inizio azione
                try {
                    repository.joinToEvent(_state.value.idEvent, _state.value.userId!!)
                    _state.update { it.copy(imSubscribe = true) }
                } finally {
                    _state.update { it.copy(isLoadingSubscription = false) } // Fine azione
                }
            }
        },
        unscribeFromEvent = {
            viewModelScope.launch {
                _state.update { it.copy(isLoadingSubscription = true) } // Inizio azione
                try {
                    repository.unscribeFromEvent(_state.value.idEvent, _state.value.userId!!)
                    _state.update { it.copy(imSubscribe = false) }
                } finally {
                    _state.update { it.copy(isLoadingSubscription = false) } // Fine azione
                }
            }
        },
        deleteEvent = {
            viewModelScope.launch {
                repository.deleteEvent(_state.value.idEvent)
            }
        }
    )
}
