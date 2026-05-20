package com.example.cacciaaltesoro.ui.screens.eventdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class EventDetailsEventState(
    val idEvent: Int = 0,
    val event: Event? = null,
    val userId: String? = null,
    val imSubscribe: Boolean = false,
    val isLoadingSubscription: Boolean = true,
    val userTagCached: Int = 0,
    val registeredUser: Int = 0,
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
                            event = repository.getEvent(id),
                            userTagCached = repository.getTagCachedByMe(id).size,
                            registeredUser = repository.getRegisteredAtEventNumber(id)?:0
                        )
                    }
                } catch (e: Exception){
                    Log.e("EventDetailsViewModel", "Errore nel caricamento evento", e)
                }
            }
        },
        saveIdUser = {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        userId = loginRepositoryImpl.getLoggedUser()?.id,
                        isLoadingSubscription = true
                    )
                }
                try {
                    val myEvents = repository.getAllMySubscribedEvents()
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
        },
        joinToEvent = {
            viewModelScope.launch {
                _state.update { it.copy(isLoadingSubscription = true) }
                try {
                    repository.joinToEvent(_state.value.idEvent)
                    _state.update { state ->
                        state.copy(
                            imSubscribe = true,
                            registeredUser = state.registeredUser + 1
                        )
                    }
                } catch (e: Exception) {
                    Log.e("EventDetailsViewModel", "Errore durante l'iscrizione", e)
                } finally {
                    _state.update { it.copy(isLoadingSubscription = false) }
                }
            }
        },
        unscribeFromEvent = {
            viewModelScope.launch {
                _state.update { it.copy(isLoadingSubscription = true) }
                try {
                    repository.unscribeFromEvent(_state.value.idEvent)
                    _state.update { state ->
                        state.copy(
                            imSubscribe = false,
                            registeredUser = if (state.registeredUser > 0) state.registeredUser - 1 else 0
                        )
                    }
                } catch (e: Exception) {
                    Log.e("EventDetailsViewModel", "Errore durante la disiscrizione", e)
                } finally {
                    _state.update { it.copy(isLoadingSubscription = false) }
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
