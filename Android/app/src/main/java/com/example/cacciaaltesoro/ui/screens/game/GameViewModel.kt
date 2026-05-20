@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.utils.nfc.NfcUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed class SheetContentState {
    object ViewingList : SheetContentState()
    class SingleTagView(val tag: Tag) : SheetContentState()
}

sealed class GameState {
    object Loading : GameState()
    data class WaitingToStart(val countDownTime: String) : GameState()
    data class Playing(val remainingTime: String) : GameState()
    data class Finished(val message: String) : GameState()
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
    private val _gameState = MutableStateFlow<GameState>(GameState.Loading)
    val gameState = _gameState.asStateFlow()

    var event : Event? = null

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _tagsToFind = MutableStateFlow<List<Tag>>(emptyList())
    val tagsToFind = _tagsToFind.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                event = eventRepository.getEventWithTags(eventId)
                if (event == null) {
                    throw IllegalArgumentException("No event with id = $eventId")
                }
                val foundTags = eventRepository.getFoundTags(eventId)
                val remainingTags = event?.tags?.filter { tag -> foundTags.none { it.id == tag.id } } ?: emptyList()
                if (remainingTags.isEmpty()) {
                    _gameState.value = GameState.Finished("Hai già trovato tutti i tag!")
                    return@launch
                }
                _tagsToFind.value = remainingTags
                startTrackingEvent()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val nfcActions = NfcActions(
        onNfcTagDiscovered = { nfcTag ->
            if (gameState.value !is GameState.Playing) return@NfcActions

            viewModelScope.launch(Dispatchers.IO) {
                val readUUID = nfcUtils.readUuidFromNdef(nfcTag)
                if (readUUID == null) {
                    _uiEvent.trySend("Tag non valido!")
                    Log.d("TAG_SCANNER", "Generic error while reading tag")
                    return@launch
                }
                val foundTag = tagsToFind.value.firstOrNull { it.id == readUUID.toString() }
                if (foundTag != null) {
                    _uiEvent.trySend("Tag ${foundTag.number} trovato!")
                    _tagsToFind.value -= foundTag
                    try {
                        eventRepository.setFoundTag(foundTag)
                    } catch (e: Exception) {
                        Log.e("DATABASE", "Error while trying to update found tag", e)
                    }
                    if (tagsToFind.value.isEmpty()) {
                        _gameState.value = GameState.Finished("Hai trovato tutti i tag!")
                        return@launch
                    }
                } else {
                    _uiEvent.trySend("Tag non valido!")
                    Log.d("TAG_SCANNER", "The scanned tag's UUID from in this event")
                }
            }
        }
    )

    fun viewSingleTag(tag: Tag) {
        _sheetContentState.value = SheetContentState.SingleTagView(tag)
    }

    fun viewTagList() {
        _sheetContentState.value = SheetContentState.ViewingList
    }

    fun startTrackingEvent() {
        viewModelScope.launch {
            while (true) {
                val now = Clock.System.now() // TODO replace with internet time for security
                val currentEvent = event ?: break

                when {
                    _gameState.value is GameState.Finished -> {
                        // Game finished by finding all tags
                        break
                    }

                    now >= currentEvent.endTime -> {
                        _gameState.value = GameState.Finished("Tempo scaduto!")
                        break
                    }

                    now >= currentEvent.startTime -> {
                        val timeString = formatTimeDifference(now, currentEvent.endTime)
                        _gameState.value = GameState.Playing(timeString)
                    }

                    else -> {
                        val timeString = formatTimeDifference(now, currentEvent.startTime)
                        _gameState.value = GameState.WaitingToStart(timeString)
                    }
                }

                delay(1000L)
            }
        }
    }

    private fun formatTimeDifference(startInstant: Instant, endInstant: Instant) : String {
        val duration = endInstant - startInstant
        val hours = duration.inWholeHours
        val minutes = duration.inWholeMinutes % 60
        val seconds = duration.inWholeSeconds % 60

        return when {
            hours > 0 -> {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            }
            else -> {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
    }
}