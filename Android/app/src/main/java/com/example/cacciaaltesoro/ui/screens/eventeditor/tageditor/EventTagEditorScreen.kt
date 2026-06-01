package com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
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

    var showDiscardChangesDialog by remember { mutableStateOf(false) }

    BackHandler {
        when (sheetContentState) {
            is SheetContentState.Editing -> viewModel.toViewingList()
            is SheetContentState.ViewingList -> {
                if (sharedViewModel.tagActions.areAllValid()) {
                    navController.popBackStack()
                } else {
                    showDiscardChangesDialog = true
                }
            }
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
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
        topBar = {
            AppBar(
                title = stringResource(R.string.new_event),
                navController = navController,
                onBackClick = {
                    if (sharedViewModel.tagActions.areAllValid()) {
                        navController.navigateUp()
                    } else {
                        showDiscardChangesDialog = true
                    }
                }
            )
        },
        sheetPeekHeight = 200.dp,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            when (sheetContentState) {
                is SheetContentState.ViewingList -> {
                    TagListContent(
                        tags = eventState.tags,
                        onTagClick = { tag ->
                            coroutineScope.launch {
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
                        isOkToSave = { viewModel.editingTagActions.isValid() },
                        onSave = {
                            sharedViewModel.tagActions.onUpdateTag(editingTag)
                            viewModel.toViewingList()
                        },
                        onDelete = {
                            sharedViewModel.tagActions.onDeleteTag(editingTag)
                            viewModel.toViewingList()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.padding(24.dp))
        }
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { stringResource ->
                snackbarHostState.showSnackbar(
                    message = stringResource.asString(context),
                    duration = SnackbarDuration.Short
                )
            }
        }

        LaunchedEffect(sheetContentState) {
            when (sheetContentState) {
                is SheetContentState.Editing -> sheetState.expand()
                is SheetContentState.ViewingList -> sheetState.partialExpand()
            }
        }

        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            GoogleMap(
                properties = MapProperties(
                    mapType = MapType.SATELLITE
                ),
                cameraPositionState = cameraPositionState,
                modifier = Modifier.fillMaxSize(),
                onMapClick = { latLng ->
                    when (sheetContentState) {
                        is SheetContentState.ViewingList -> {
                            val newTag = sharedViewModel.tagActions.onNewTag(latLng.toCoordinates())
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(latLng)
                                )
                            }
                            viewModel.toEditing(newTag)
                        }
                        is SheetContentState.Editing -> { viewModel.toViewingList() }
                    }
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Marker(
                    state = rememberUpdatedMarkerState(position = LatLng(startingLat, startingLon)),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    title = stringResource(R.string.meeting_point)
                )
                eventState.tags.forEach { tag ->
                    Marker(
                        rememberUpdatedMarkerState(position = tag.coordinates.toLatLng()),
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(tag.coordinates.toLatLng())
                                )
                            }
                            viewModel.toEditing(tag)
                            true
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (tag.hasNfc) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }
        }

        when (nfcState) {
            is NfcState.WaitingForTag -> {
                AlertDialog(
                    onDismissRequest = { viewModel.nfcActions.resetState() },
                    icon = { Icon(Icons.Default.Nfc, contentDescription = null) },
                    title = { Text(stringResource(R.string.nfc_assoc_title)) },
                    text = { Text(stringResource(R.string.nfc_assoc_body)) },
                    confirmButton = {
                        Button(onClick = { viewModel.nfcActions.resetState() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    properties = DialogProperties(dismissOnClickOutside = false)
                )
            }
            is NfcState.Disabled -> {
                AlertDialog(
                    onDismissRequest = { viewModel.nfcActions.resetState() },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    title = { Text(stringResource(R.string.nfc_disabled_title)) },
                    text = { Text(stringResource(R.string.nfc_disabled_body)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                                context.startActivity(intent)
                                viewModel.nfcActions.resetState()
                            }
                        ) { Text(stringResource(R.string.open_settings)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.nfcActions.resetState() } ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    properties = DialogProperties(dismissOnClickOutside = false)
                )
            }
            else -> {}
        }

        if (showDiscardChangesDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardChangesDialog = false },
                title = { Text(stringResource(R.string.invalid_tags_title)) },
                text = { Text(stringResource(R.string.invalid_tags_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDiscardChangesDialog = false
                        navController.popBackStack()
                    }) {
                        Text(stringResource(R.string.quit_anyways))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDiscardChangesDialog = false }) {
                        Text(stringResource(R.string.keep_editing))
                    }
                }
            )
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
                stringResource(R.string.no_inserted_tags_msg),
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
                        Text(stringResource(R.string.tag_number_symbol) + tag.number)
                    },
                    leadingContent = {
                        Icon(
                            if (tag.hasNfc) Icons.Default.Done else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (tag.hasNfc) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { onDeleteTag(tag) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
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
        Spacer(modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun TagEditor(
    tag: Tag,
    onAssociateNfcTag: () -> Unit,
    onChangeHint: (String) -> Unit,
    isOkToSave: () -> Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tag_number_symbol) + tag.number,
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
                Text(stringResource(R.string.link_nfc_tag))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (tag.hasNfc) Icons.Default.Done else Icons.Default.Warning,
                contentDescription = null,
                tint = if (tag.hasNfc) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Text(
                text = if (tag.hasNfc) {
                    stringResource(R.string.nfc_linked)
                } else {
                    stringResource(R.string.nfc_not_linked)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.padding(1.dp))

        OutlinedTextField(
            value = tag.textHint ?: "",
            onValueChange = onChangeHint,
            label = { Text(stringResource(R.string.hint) + " " + stringResource(R.string.optional_par)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onDelete() },
                modifier = Modifier.weight(0.5f)
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.weight(0.5f),
                onClick = { onSave() },
                enabled = isOkToSave()
            ) {
                Text(stringResource(R.string.save))
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))
    }
}