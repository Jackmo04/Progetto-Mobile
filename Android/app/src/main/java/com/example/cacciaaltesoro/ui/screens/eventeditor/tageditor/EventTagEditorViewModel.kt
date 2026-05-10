package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import androidx.lifecycle.ViewModel
import com.example.cacciaaltesoro.data.domain.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class TagScreenState {
    object ViewingList : TagScreenState()
//    data class AddingDetails(val location: LatLng) : TagScreenState()
    data class Editing(val tag: Tag) : TagScreenState()
}

class EventTagEditorViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<TagScreenState>(TagScreenState.ViewingList)
    val screenState = _screenState.asStateFlow()

    fun returnToViewingList() {
        _screenState.value = TagScreenState.ViewingList
    }

}