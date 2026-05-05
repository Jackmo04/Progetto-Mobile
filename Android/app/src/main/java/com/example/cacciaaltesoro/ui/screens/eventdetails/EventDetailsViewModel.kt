package com.example.cacciaaltesoro.ui.screens.eventdetails

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.EventDetailsRepository
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class EventDetailsEventState(
    val idEvent: Int,
    val  event: EventDTO?
)

data class EventDetailsEventAction(
    val findEventByID: (Int) -> Unit
)

class EventDetailsViewModel(
    private val repository: EventDetailsRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private var _state by mutableStateOf(EventDetailsEventState(
        idEvent = TODO(),
        event = TODO()
    ))
    fun getState() = _state

    init {
        viewModelScope.launch {



        }
    }

    @OptIn(ExperimentalTime::class)
    val action = EventDetailsEventAction(
        findEventByID = { id ->
            viewModelScope.launch {
                try {
                    _state = _state.copy(event = repository.getEvents(id))
                }catch (e: Exception){

                }
            }
        }

    )
}
