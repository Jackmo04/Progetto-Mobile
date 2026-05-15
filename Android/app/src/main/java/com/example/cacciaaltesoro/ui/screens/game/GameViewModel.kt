package com.example.cacciaaltesoro.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.utils.nfc.NfcUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SheetContentState {
    object ViewingList : SheetContentState()
    class SingleTagView(val tag: Tag) : SheetContentState()
}

sealed class GameState {
    object WaitingToStart : GameState()
    object Playing : GameState()
    object Finished : GameState()
}

class NfcActions(
    val onNfcTagDiscovered: (android.nfc.Tag) -> Unit
)

class GameViewModel(
    private val eventRepository: EventRepository,
    private val nfcUtils: NfcUtils,
    private val eventId: Int
) : ViewModel() {
    private val _sheetContentState = MutableStateFlow<SheetContentState>(SheetContentState.ViewingList)
    val sheetContentState = _sheetContentState.asStateFlow()
    private val _gameState = MutableStateFlow(GameState.Playing) // TODO change into 'WaitingToStart'
    val gameState = _gameState.asStateFlow()

    var event : Event? = null

    private val _tagsToFind = MutableStateFlow<List<Tag>>(emptyList())
    val tagsToFind = _tagsToFind.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                event = eventRepository.getEventWithTags(eventId)
                if (event == null) {
                    throw IllegalArgumentException("No event with id = $eventId")
                }
                _tagsToFind.value = event!!.tags
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val nfcActions = NfcActions(
        onNfcTagDiscovered = { nfcTag ->

        }
    )

    fun viewSingleTag(tag: Tag) {
        _sheetContentState.value = SheetContentState.SingleTagView(tag)
    }

    fun viewTagList() {
        _sheetContentState.value = SheetContentState.ViewingList
    }

}