@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.ClickableBox
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar
import kotlin.time.ExperimentalTime

@Composable
fun NewEventScreen(
    navController: NavHostController,
    viewModel: NewEventViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var showMapDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.new_event), navController) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Event name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.actions.onNameChange(it) },
                label = {
                    Text("${stringResource(R.string.name)} ${stringResource(R.string.optional_par)}")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Event description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.actions.onDescriptionChange(it) },
                label = { Text("${stringResource(R.string.description)} ${stringResource(R.string.optional_par)}") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Location
            ClickableBox(
                onClick = { showMapDialog = true }
            ) {
                OutlinedTextField(
                    value = state.location?.let {
                        """${it.latitude}
                       |${it.longitude}""".trimMargin()
                    } ?: "",
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

            // Start Date and Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.actions.onStartDateChange(year, month + 1, dayOfMonth)
                            },
                            state.startDate.year,
                            state.startDate.monthValue - 1,
                            state.startDate.dayOfMonth
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.fStartDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.start_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.CalendarMonth,
                            stringResource(R.string.choose)
                        )},
                        isError = state.isImpossibleStartDateTime
                    )
                }

                // Time
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.actions.onStartTimeChange(hour, minute)
                            },
                            state.startTime.hour,
                            state.startTime.minute,
                            true
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.fStartTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.start_time)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.AccessTime,
                            stringResource(R.string.choose)
                        )},
                        isError = state.isImpossibleStartDateTime
                    )
                }
            }

            // End Date and Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.actions.onEndDateChange(year, month + 1, dayOfMonth)
                            },
                            state.endDate.year,
                            state.endDate.monthValue - 1,
                            state.endDate.dayOfMonth
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.fEndDate.ifBlank { state.fStartDate },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.end_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.CalendarMonth,
                            stringResource(R.string.choose)
                        )},
                        isError = state.isImpossibleEndDateTime
                    )
                }

                // Time
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.actions.onEndTimeChange(hour, minute)
                            },
                            state.endTime.hour,
                            state.endTime.minute,
                            true
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.fEndTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.end_time)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.AccessTime,
                            stringResource(R.string.choose)
                        )},
                        supportingText = { Text("${stringResource(R.string.timezone_abbr)}: ${state.timeZone}") },
                        isError = state.isImpossibleEndDateTime
                    )
                }
            }

            // TODO add other fields

            // Submit
            Button(
                onClick = { viewModel.actions.onSaveEvent() },
                enabled = state.location != null
                        && !state.isImpossibleStartDateTime
                        && !state.isImpossibleEndDateTime
            ) {
                Text(stringResource(R.string.create_event))
            }
        }

        if (showMapDialog) {
            MapPickerDialog (
                startingMarkerPosition = state.location?.toLatLng(),
                onDismiss = { showMapDialog = false },
                onLocationSelected = { latLng ->
                    viewModel.actions.onLocationChange(latLng.toCoordinates())
                    showMapDialog = false
                }
            )
        }
    }
}

@Composable
fun MapPickerDialog(
    startingMarkerPosition: LatLng? = null,
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
                        startingMarkerPosition ?: LatLng(44.148, 12.236),
                        13f
                    )
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
                            mapToolbarEnabled = false
                            //myLocationButtonEnabled = true // TODO get permissions
                        ),
                        onMapLoaded = { isMapLoaded = true },
                        onMapClick = { latLng ->
                            markerPosition = latLng
                            hasSelectedLocation = true
                        }
                    ) {
                        markerPosition?.let { latLng ->
                            Marker(
                                state = MarkerState(position = latLng)
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