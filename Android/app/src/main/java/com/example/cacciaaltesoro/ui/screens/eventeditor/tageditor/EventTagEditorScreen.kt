package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
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
import com.example.cacciaaltesoro.utils.nfc.NfcReaderLifecycle
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
    val sheetContentState by viewModel.sheetContentState.collectAsStateWithLifecycle()
    val nfcState by viewModel.nfcState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    NfcReaderLifecycle(
        isActive = nfcState !is NfcState.Idle,
        onTagDiscovered = { nfcTag ->
            viewModel.nfcActions.onNfcTagDiscovered(nfcTag)
        },
        onNfcDisabled = { viewModel.nfcActions.onNfcDisabled() }
    )

    val editingTag by viewModel.editingTag.collectAsStateWithLifecycle()

    BackHandler(enabled = sheetContentState !is SheetContentState.ViewingList) {
        // TODO logica per rimuovere il tag se non salvato
        viewModel.toViewingList()
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(startingLat, startingLon),
            18f
        )
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(stringResource(R.string.new_event), navController) },
        sheetPeekHeight = 200.dp,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            Crossfade(targetState = sheetContentState, label = "sheet_content") { state ->
                when (state) {
                    is SheetContentState.ViewingList -> {
                        TagListContent(
                            tags = eventState.tags,
                            onTagClick = { tag ->
                                coroutineScope.launch {
                                    sheetState.expand()
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(tag.coordinates.toLatLng())
                                    )
                                }
                                viewModel.toEditing(tag)
                            },
                            onDeleteTag = { tag ->
                                sharedViewModel.tagActions.onDeleteTag(tag)
                            }
                        )
                    }
                    is SheetContentState.Editing -> {
                        TagEditor(
                            tag = editingTag,
                            onAssociateNfcTag = {
                                viewModel.nfcActions.prepareForWrite()
                            },
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
        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }

        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            GoogleMap(
                properties = MapProperties(
                    mapType = MapType.SATELLITE
                ),
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                onMapClick = { latLng ->
                    if (sheetContentState is SheetContentState.ViewingList) {
                        val newTag = sharedViewModel.tagActions.onNewTag(latLng.toCoordinates())
                        viewModel.toEditing(newTag)
                        coroutineScope.launch {
                            sheetState.expand()
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLng(latLng)
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
                        MarkerState(position = tag.coordinates.toLatLng()),
                        onClick = {
                            viewModel.toEditing(tag)
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(tag.coordinates.toLatLng())
                                )
                            }
                            true
                        }
                    )
                }
            }
        }

        when (nfcState) {
            is NfcState.WaitingForTag -> {
                AlertDialog(
                    onDismissRequest = { viewModel.nfcActions.resetState() },
                    icon = { Icon(Icons.Default.Nfc, contentDescription = null) },
                    title = { Text("Scrittura Tag NFC") },
                    text = { Text("Avvicina il tag NFC al retro del dispositivo per completare l'associazione.") },
                    confirmButton = {
                        Button(onClick = { viewModel.nfcActions.resetState() }) {
                            Text("Annulla")
                        }
                    }
                )
            }
            is NfcState.Disabled -> {
                AlertDialog(
                    onDismissRequest = { viewModel.nfcActions.resetState() },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    title = { Text("NFC Disattivato!") },
                    text = { Text("Per poter continuare è necessario abilitare l'NFC dalle impostazioni di sistema.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                                context.startActivity(intent)
                                viewModel.nfcActions.resetState()
                            }
                        ) { Text("Apri impostazioni") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.nfcActions.resetState() } ) {
                            Text("Annulla")
                        }
                    }
                )
            }
            else -> {}
        }

    }
}

@Composable
fun TagListContent(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    onDeleteTag: (Tag) -> Unit
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Modifica tesoro",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { onDeleteTag(tag) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina")
                            }
                        }
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
    onAssociateNfcTag: () -> Unit,
    onChangeHint: (String) -> Unit,
    onChangeImage: (String) -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tag N° ${tag.number}",
            style = MaterialTheme.typography.headlineSmall
        )

        HorizontalDivider()

        FilledTonalButton(
            onClick = { onAssociateNfcTag() }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Nfc, contentDescription = null)
                Text("Associa tag NFC")
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.padding(1.dp))

        Text(
            text = "Indizi (Opzionali)",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = tag.textHint ?: "",
            onValueChange = onChangeHint,
            label = { Text("Indizio testuale") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        FilledTonalButton(
            onClick = {/* TODO */}
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Text("Indizio visivo")
            }

        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSave() },
            enabled = true // TODO check for nfc
        ) {
            Text("Salva")
        }
    }
}