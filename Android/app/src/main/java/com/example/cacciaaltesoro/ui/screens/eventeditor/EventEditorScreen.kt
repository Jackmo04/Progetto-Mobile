@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.eventeditor

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.mappers.toCoordinates
import com.example.cacciaaltesoro.data.mappers.toLatLng
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.ClickableBox
import com.example.cacciaaltesoro.utils.Coordinates
import com.example.cacciaaltesoro.utils.LocationService
import com.example.cacciaaltesoro.utils.PermissionStatus
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@Composable
fun EventEditorScreen(
    navController: NavHostController,
    viewModel: EventEditorViewModel
) {
    val eventState by viewModel.eventState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    var showMapDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val ctx = LocalContext.current
    val locationService = remember { LocationService(ctx) }

    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    fun getCurrentLocation() = scope.launch {
        try {
            locationService.getCurrentLocation()
        } catch (_: Exception) {}
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { permissionResults ->
        val isGrantedNow = permissionResults.values.any { it.isGranted }
        if (isGrantedNow) {
            getCurrentLocation()
        }
    }

    fun getLocationOrRequestPermission() {
        if (locationPermissions.statuses.any { it.value.isGranted }) {
            getCurrentLocation()
        } else {
            locationPermissions.launchPermissionRequest()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = if (uiState.isEditMode) stringResource(R.string.edit_event)
                        else stringResource(R.string.new_event),
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Event name
            OutlinedTextField(
                value = eventState.name,
                onValueChange = { viewModel.eventActions.onNameChange(it) },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Event description
            OutlinedTextField(
                value = eventState.description,
                onValueChange = { viewModel.eventActions.onDescriptionChange(it) },
                label = { Text("${stringResource(R.string.description)} ${stringResource(R.string.optional_par)}") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Location
            ClickableBox(
                onClick = {
                    showMapDialog = true
                }
            ) {
                OutlinedTextField(
                    value = eventState.fLocation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.meeting_point)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(
                        Icons.Default.Place,
                        stringResource(R.string.choose)
                    )}
                )
            }

            HorizontalDivider()

            // Start DateTime
            //DateTimeInputs1(state, viewModel) // old design
            DateTimeInputs2(eventState, viewModel)

            HorizontalDivider()

            // Visibility
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${stringResource(R.string.visibility)}:",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow {
                    Visibility.entries.forEachIndexed { index, visibility ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults
                                .itemShape(index = index, count = Visibility.entries.size),
                            onClick = { viewModel.eventActions.onVisibilityChange(visibility) },
                            selected = visibility == eventState.visibility,
                            icon = {
                                Icon(
                                    when (visibility) {
                                        Visibility.PUBLIC -> Icons.Default.Public
                                        Visibility.PRIVATE -> Icons.Default.Lock
                                    },
                                    contentDescription = null
                                )
                            }
                        ) {
                            Text(stringResource(visibility.labelRes))
                        }
                    }
                }
            }

            HorizontalDivider()

            // Manage tags
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${stringResource(R.string.tag_number)}: ${eventState.tags.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                FilledTonalButton(
                    onClick = {
                        if (viewModel.eventActions.onEditTagsClick()) {
                            eventState.location?.let {
                                navController.navigate(NavigationRoute.EventTagEditor(
                                    it.latitude,
                                    it.longitude
                                ))
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.PinDrop, contentDescription = null)
                    Text(stringResource(R.string.manage_tags))
                }
            }

            // Submit
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.eventActions.onSaveEvent()
                    focusManager.clearFocus()
                },
                enabled = eventState.location != null
                        && eventState.name.isNotBlank()
                        && !eventState.isImpossibleStartDateTime
                        && !eventState.isImpossibleEndDateTime
                        && !uiState.isLoading
            ) {
                Text(
                    if (uiState.isEditMode) stringResource(R.string.save_changes)
                    else stringResource(R.string.create_event)
                )
            }
        }

        if (showMapDialog) {
            getLocationOrRequestPermission()
            MapPickerDialog (
                startingMarkerPosition = eventState.location?.toLatLng(),
                startingCameraPosition = eventState.location?.toLatLng() ?: coordinates?.toLatLng(),
                showCurrentLocation = locationPermissions.statuses.any { it.value.isGranted },
                onDismiss = { showMapDialog = false },
                onLocationSelected = { latLng ->
                    viewModel.eventActions.onLocationChange(latLng.toCoordinates())
                    showMapDialog = false
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
fun DateTimeInputs2(
    state: EventState,
    viewModel: EventEditorViewModel
) {
    val context = LocalContext.current

    val errorColor = MaterialTheme.colorScheme.error
    val okContentColor = MaterialTheme.colorScheme.secondary
    val okBorderColor = MaterialTheme.colorScheme.outline

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${stringResource(R.string.start)}:", style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.eventActions.onStartDateChange(year, month + 1, dayOfMonth)
                            },
                            state.startDate.year,
                            state.startDate.monthValue - 1,
                            state.startDate.dayOfMonth
                        ).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.isImpossibleStartDateTime) errorColor else okContentColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (state.isImpossibleStartDateTime) errorColor else okBorderColor
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.date)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(state.fStartDate)
                }

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.eventActions.onStartTimeChange(hour, minute)
                            },
                            state.startTime.hour,
                            state.startTime.minute,
                            true
                        ).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.isImpossibleStartDateTime) errorColor else okContentColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (state.isImpossibleStartDateTime) errorColor else okBorderColor
                    )
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = stringResource(R.string.time)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(state.fStartTime)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${stringResource(R.string.end)}:", style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.eventActions.onEndDateChange(year, month + 1, dayOfMonth)
                            },
                            state.endDate.year,
                            state.endDate.monthValue - 1,
                            state.endDate.dayOfMonth
                        ).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.isImpossibleEndDateTime) errorColor else okContentColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (state.isImpossibleEndDateTime) errorColor else okBorderColor
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.date)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(state.fEndDate)
                }

                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.eventActions.onEndTimeChange(hour, minute)
                            },
                            state.endTime.hour,
                            state.endTime.minute,
                            true
                        ).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (state.isImpossibleEndDateTime) errorColor else okContentColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (state.isImpossibleEndDateTime) errorColor else okBorderColor
                    )
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = stringResource(R.string.time)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(state.fEndTime)
                }
            }
        }

        Spacer(modifier = Modifier.padding(vertical = 2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "${stringResource(R.string.timezone)}: ${state.timeZone}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun MapPickerDialog(
    startingMarkerPosition: LatLng? = null,
    startingCameraPosition: LatLng?,
    showCurrentLocation: Boolean,
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        startingCameraPosition ?: LatLng(44.148, 12.236),
                        16f
                    )
                }

                LaunchedEffect(startingCameraPosition) {
                    startingCameraPosition?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 16f)
                    }
                }

                var isMapLoaded by remember { mutableStateOf(false) }
                var markerPosition by remember { mutableStateOf(startingMarkerPosition) }
                var hasSelectedLocation by remember { mutableStateOf(startingMarkerPosition != null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            mapToolbarEnabled = false,
                            myLocationButtonEnabled = showCurrentLocation
                        ),
                        properties = MapProperties(
                            isMyLocationEnabled = showCurrentLocation
                        ),
                        onMapLoaded = { isMapLoaded = true },
                        onMapClick = { latLng ->
                            markerPosition = latLng
                            hasSelectedLocation = true
                        }
                    ) {
                        markerPosition?.let { latLng ->
                            Marker(
                                state = MarkerState(position = latLng),
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }
                    }

                    if (!isMapLoaded) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = { markerPosition?.let { onLocationSelected(it) } },
                        enabled = isMapLoaded && hasSelectedLocation
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}