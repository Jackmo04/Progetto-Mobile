package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.mappers.toCoordinates
import com.example.cacciaaltesoro.data.mappers.toLatLng
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.screens.eventeditor.EventEditorViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTagEditorScreen(
    navController: NavHostController,
    sharedViewModel: EventEditorViewModel,
    viewModel: EventTagEditorViewModel,
    startingLat: Double,
    startingLon: Double
) {
    val eventState by sharedViewModel.eventState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val editingTag by viewModel.editingTag.collectAsStateWithLifecycle()

    BackHandler(enabled = screenState !is TagScreenState.ViewingList) {
        // TODO logica per rimuovere il tag se non salvato
        viewModel.toViewingList()
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(startingLat, startingLon),
            16f
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(stringResource(R.string.new_event), navController) },
        sheetPeekHeight = 120.dp,
        sheetContent = {
            Crossfade(targetState = screenState, label = "sheet_content") { state ->
                when (state) {
                    is TagScreenState.ViewingList -> {
                        TagListContent(
                            tags = eventState.tags,
                            onTagClick = { tag ->
                                coroutineScope.launch {
                                    sheetState.expand()
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            tag.coordinates.toLatLng(),
                                            16f
                                        )
                                    )
                                }
                                viewModel.toEditing(tag)
                            }
                        )
                    }
                    is TagScreenState.Editing -> {
                        TagEditor(
                            tag = editingTag,
                            onChangeHint = { newHint ->
                                viewModel.editingTagActions.onTextHintChange(newHint)
                            },
                            onChangeImage = { newImageUri ->
                                viewModel.editingTagActions.onImageHintChange(newImageUri)
                            },
                            onSave = {
                                sharedViewModel.tagActions.onUpdateTag(editingTag)
                                viewModel.toViewingList()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(24.dp))
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                properties = MapProperties(
                    mapType = MapType.SATELLITE
                ),
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                onMapClick = { latLng ->
                    if (screenState is TagScreenState.ViewingList) {
                        val newTag = sharedViewModel.tagActions.onNewTag(latLng.toCoordinates())
                        viewModel.toEditing(newTag)
                        coroutineScope.launch {
                            sheetState.expand()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng,
                                    16f
                                )
                            )
                        }
                    }
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Marker(
                    MarkerState(position = LatLng(startingLat, startingLon)),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
                eventState.tags.forEach { tag ->
                    Marker(
                        MarkerState(position = tag.coordinates.toLatLng())
                    )
                }
            }
        }

    }
}

@Composable
fun TagListContent(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit
) {
    if (tags.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Ancora nessun tag inserito.\nClicca sulla mappa per posizionarli!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(tags) { tag ->
                ListItem(
                    headlineContent = {
                        Text("Tag N° ${tag.number}")
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Modifica tesoro",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { onTagClick(tag) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
fun TagEditor(
    tag: Tag,
    onChangeHint: (String) -> Unit,
    onChangeImage: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Modifica Tag N° ${tag.number}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = tag.textHint ?: "",
            onValueChange = onChangeHint,
            label = { Text("Indizio") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {/* TODO */}
        ) {
            Text("Seleziona immagine")
        }
        // TODO add nfc writer

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSave() },
            enabled = true // TODO check for nfc
        ) {
            Text("Salva")
        }
    }
}