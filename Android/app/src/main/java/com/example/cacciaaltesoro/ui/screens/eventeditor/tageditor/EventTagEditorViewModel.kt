package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import com.example.cacciaaltesoro.utils.nfc.NfcUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed class SheetContentState {
    object ViewingList : SheetContentState()
    object Editing : SheetContentState()
}

sealed class NfcState {
    object Idle : NfcState()
    object WaitingForTag : NfcState()
    object Done : NfcState()
}

data class EditingTagActions(
    val onTextHintChange: (String) -> Unit,
    val onImageHintChange: (String) -> Unit
)

data class NfcActions(
    val onNfcTagDiscovered: (android.nfc.Tag) -> Unit,
    val prepareForWrite: () -> Unit,
    val resetState: () -> Unit
)

class EventTagEditorViewModel(private val nfcUtils: NfcUtils) : ViewModel() {
    private val _sheetContentState = MutableStateFlow<SheetContentState>(SheetContentState.ViewingList)
    val sheetContentState = _sheetContentState.asStateFlow()

    private val _nfcState = MutableStateFlow<NfcState>(NfcState.Idle)
    val nfcState = _nfcState.asStateFlow()

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _editingTag = MutableStateFlow(Tag(
        number = 0,
        coordinates = Coordinates(0.0,0.0)
    ))
    val editingTag = _editingTag.asStateFlow()

    val editingTagActions = EditingTagActions(
        onTextHintChange = { newHint ->
            _editingTag.update { it.copy(textHint = newHint) }
        },
        onImageHintChange = { newImage ->
            _editingTag.update { it.copy(imageHint = newImage) }
        }
    )

    val nfcActions = NfcActions(
        onNfcTagDiscovered = { nfcTag ->
            viewModelScope.launch(Dispatchers.IO) {
                val result = nfcUtils.writeUuidToNdef(nfcTag, UUID.fromString(_editingTag.value.id))
                if (result) {
                    _uiEvent.trySend("Tag NFC scritto correttamente!")
                    Log.d("NFC_DEBUG", "Scritto: ${_editingTag.value.id}")
                } else {
                    _uiEvent.trySend("Errore! Riprova per favore!")
                }
                _nfcState.value = NfcState.Done // TODO improve logic if there's time
                delay(2000L)
                _nfcState.value = NfcState.Idle
            }
        },
        prepareForWrite = {
            _nfcState.value = NfcState.WaitingForTag
            //_uiEvent.trySend("Avvicina il tag NFC") // TODO change in ui
        },
        resetState = {
            _nfcState.value = NfcState.Idle
        }
    )

    fun toViewingList() {
        _sheetContentState.value = SheetContentState.ViewingList
    }

    fun toEditing(tag: Tag) {
        _editingTag.value = tag
        _sheetContentState.value = SheetContentState.Editing
    }

}