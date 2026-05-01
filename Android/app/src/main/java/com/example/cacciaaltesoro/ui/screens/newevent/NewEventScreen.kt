@file:OptIn(ExperimentalTime::class)

package com.example.cacciaaltesoro.ui.screens.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.ClickableBox
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Composable
fun NewEventScreen(
    navController: NavHostController,
    viewModel: NewEventViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val dateFormatter = remember {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
    }

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
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            // Event description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.actions.onDescriptionChange(it) },
                label = { Text("${stringResource(R.string.description)} ${stringResource(R.string.optional_par)}") },
                modifier = Modifier.fillMaxWidth()
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

            // Date and Time
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                viewModel.actions.onStartDateChange(year, month, dayOfMonth)
                            },
                            state.startDate?.year ?: calendar.get(Calendar.YEAR),
                            state.startDate?.monthValue ?: calendar.get(Calendar.MONTH),
                            state.startDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.date)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.DateRange,
                            stringResource(R.string.choose)
                        )}
                    )
                }

                // Time
                ClickableBox(
                    modifier = Modifier.weight(0.5f),
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                viewModel.actions.onStartTimeChange(hour, minute)
                            },
                            state.startTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY),
                            state.startTime?.minute ?: calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }
                ) {
                    OutlinedTextField(
                        value = state.formattedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.time)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(
                            Icons.Default.AccessTime,
                            stringResource(R.string.choose)
                        )},
                        supportingText = { Text("${stringResource(R.string.timezone_abbr)}: ${state.timeZone}") }
                    )
                }
            }

            // TODO add other fields

            // Submit
            Button(
                onClick = { viewModel.actions.onSaveEvent() },
                enabled = state.location != null
                        && state.startDate != null
                        && state.startTime != null
            ) {
                Text(stringResource(R.string.create_event))
            }
        }

        if (showMapDialog) {
            MapPickerDialog (
                startingMarkerPosition = state.location,
                onDismiss = { showMapDialog = false },
                onLocationSelected = { latLng ->
                    viewModel.actions.onLocationChange(latLng)
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