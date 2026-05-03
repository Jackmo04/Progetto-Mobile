package com.example.cacciaaltesoro.ui.screens.onlineevents

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import kotlinx.coroutines.launch

data class OnlineEventState(
    val ListEvent: List<EventDTO> = emptyList()
)

data class OnlineEventAction(
    val onSearchEvent: (String) -> Unit,
    val onViewEvent: (String) -> Unit
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

                _state = _state.copy(ListEvent = repository.getAllEvents("%"))

        }
    }

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

        }
    )
}
