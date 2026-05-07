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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class EventDetailsEventState(
    val idEvent: Int = 0,
    val event: EventDTO? = null,
    val userId: String? = null,
    val imSubscribe: Boolean = false
)

data class EventDetailsEventAction(
    val findEventByID: (Int) -> Unit,
    val saveIdUser: () -> Unit,
    val joinToEvent:() -> Unit,
    val unscribeFromEvent:() -> Unit
)

class EventDetailsViewModel(
    private val repository: EventRepository,
    private val loginRepositoryImpl: LoginRepositoryImpl
) : ViewModel() {

    private var _state by mutableStateOf(EventDetailsEventState(
    ))
    fun getState() = _state

    init {


    }

    @OptIn(ExperimentalTime::class)
    val action = EventDetailsEventAction(
        findEventByID = { id ->
            viewModelScope.launch {
                try {
                    _state = _state.copy(event = repository.getEvent(id))
                }catch (e: Exception){

                }
            }
        },
        saveIdUser = {
            viewModelScope.launch {
                loginRepositoryImpl.userId.collect { userId ->
                    _state = _state.copy(userId = userId)
                    try {
                        val myEvents = repository.getAllMyEvents(userId!!)
                        val isSubscribed = myEvents.any { it.id == _state.idEvent }
                        _state = _state.copy(imSubscribe = isSubscribed)
                    } catch (e: Exception) {
                        Log.e("EventDetailsViewModel", "Error checking subscription: ${e.message}")
                    }
                }

            }
        },
        joinToEvent = {
            viewModelScope.launch {

                repository.joinToEvent(_state.idEvent, _state.userId!!)
                _state = _state.copy(imSubscribe = true)
            }
        },
        unscribeFromEvent = {
            viewModelScope.launch {
                repository.unscribeFromEvent(_state.idEvent, _state.userId!!)
                _state = _state.copy(imSubscribe = false)
            }
        }
    )
}
