package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import androidx.lifecycle.ViewModel
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.domain.utils.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class TagScreenState {
    object ViewingList : TagScreenState()
    object Editing : TagScreenState()
}

data class EditingTagActions(
    val onTextHintChange: (String) -> Unit,
    val onImageHintChange: (String) -> Unit
)

class EventTagEditorViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<TagScreenState>(TagScreenState.ViewingList)
    val screenState = _screenState.asStateFlow()

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

    fun toViewingList() {
        _screenState.value = TagScreenState.ViewingList
    }

    fun toEditing(tag: Tag) {
        _editingTag.value = tag
        _screenState.value = TagScreenState.Editing
    }
}