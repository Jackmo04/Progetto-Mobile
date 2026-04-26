package com.example.cacciaaltesoro.ui.screens.newevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun NewEventScreen(
    navController: NavHostController,
    viewModel: NewEventViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showMapDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.new_event), navController) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.actions.onNameChange(it) },
                label = { Text(stringResource(R.string.event_name)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.actions.onDescriptionChange(it) },
                label = { Text(stringResource(R.string.description)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

//            OutlinedTextField(
//                value = state.location?.let { "${it.latitude}, ${it.longitude}" } ?: "",
//                onValueChange = {},
//                readOnly = true,
//                label = { Text(stringResource(R.string.meeting_point)) },
//                trailingIcon = {
//                    IconButton(onClick = { showMapDialog = true }) {
//                        Icon(
//                            Icons.Default.LocationOn,
//                            contentDescription = stringResource(R.string.open_map)
//                        )
//                    }
//                },
//                modifier = Modifier.clickable { showMapDialog = true }
//            )

            Button(onClick = { showMapDialog = true }) {
                Text(stringResource(R.string.choose_meeting_point))
            }

            if (state.location != null) {
                OutlinedTextField(
                    value = state.location?.let {
                        """${it.latitude}
                           |${it.longitude}""".trimMargin()
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.meeting_point)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))





            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.actions.onSaveEvent() },
                enabled = state.name.isNotBlank()
                        && state.description.isNotBlank()
                        && state.location != null
                        && state.startDateTime.isNotBlank()
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
    startingMarkerPosition: LatLng?,
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
                val markerState = rememberUpdatedMarkerState(
                    startingMarkerPosition ?: LatLng(44.148, 12.236)
                )
                var isMapLoaded by remember { mutableStateOf(false) }
                var hasSelectedLocation by remember { mutableStateOf(startingMarkerPosition != null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapLoaded = { isMapLoaded = true },
                        onMapClick = { latLng ->
                            markerState.position = latLng
                            hasSelectedLocation = true
                        }
                    ) {
                        if (hasSelectedLocation) {
                            Marker(markerState)
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
                        onClick = { onLocationSelected(markerState.position) },
                        enabled = isMapLoaded && hasSelectedLocation
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}