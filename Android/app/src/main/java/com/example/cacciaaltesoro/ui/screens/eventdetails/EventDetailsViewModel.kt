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
    val userId: String? = null
)

data class EventDetailsEventAction(
    val findEventByID: (Int) -> Unit,
    val saveIdUser: () -> Unit
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
                    Log.i("CardEvent", _state.userId!!)
                }

            }
        }
    )
}
