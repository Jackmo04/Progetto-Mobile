package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import androidx.lifecycle.ViewModel
import com.example.cacciaaltesoro.data.domain.Tag
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class TagScreenState {
    object ViewingList : TagScreenState()
//    data class AddingDetails(val location: LatLng) : TagScreenState()
    data class Editing(val tag: Tag) : TagScreenState()
}

class EventTagEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<TagScreenState>(TagScreenState.ViewingList)
    val uiState = _uiState.asStateFlow()

}