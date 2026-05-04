package com.example.cacciaaltesoro.ui.screens.onlineevents

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import com.example.cacciaaltesoro.utils.EventOrderType
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class OnlineEventState(
    val ListEvent: List<EventDTO> = emptyList()
)

data class OnlineEventAction(
    val onSearchEvent: (String) -> Unit,
    val onViewEvent: (String) -> Unit,
    val onOrderChanged: (String) -> Unit
)

class OnlineEventViewModel(
    private val repository: OnlineEventRepository
) : ViewModel() {

    private var _state by mutableStateOf(OnlineEventState())
    fun getState() = _state
    fun onOrderChanged(selected: String) {}

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
                _state = _state.copy(ListEvent = repository.getAllEvents("%"))
            }finally {
                isLoading = false
            }


        }
    }

    @OptIn(ExperimentalTime::class)
    val action = OnlineEventAction(
        onSearchEvent = { query ->
            viewModelScope.launch {
                isLoading = true
                try {
                    _state = _state.copy(ListEvent = repository.getAllEvents(query))
                } catch (e: Exception) {
                    errorMessage = "Errore durante la ricerca"
                } finally {
                    isLoading = false
                }
            }
        },
        onViewEvent = {

        },
        onOrderChanged = { selected ->
            viewModelScope.launch {
                //Log.i("Orderd" , _state.ListEvent.toString())
                when (selected) {
                    EventOrderType.NAME.type -> {
                        _state = _state.copy(ListEvent = _state.ListEvent.sortedBy { it.name })
                    }
                    EventOrderType.START_DATE.type -> {
                        _state = _state.copy(ListEvent = _state.ListEvent.sortedBy { it.startTime.epochSeconds })
                    Log.i("Orderd" , _state.ListEvent.toString())
                    }
                    EventOrderType.EVENT_DURATION.type -> {
                        _state = _state.copy(ListEvent = _state.ListEvent.sortedBy { it.endTime.nanosecondsOfSecond - it.startTime.nanosecondsOfSecond })
                    }

                    EventOrderType.DISTANCE.type -> {
                       // _state = _state.copy(ListEvent = _state.ListEvent.sortedBy { it.distance })
                    }
                }
            }
        }
    )
}
