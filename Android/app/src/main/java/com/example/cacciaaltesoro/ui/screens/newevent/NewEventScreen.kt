package com.example.cacciaaltesoro.ui.screens.newevent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun NewEventScreen(
    navController: NavHostController,
    viewModel: NewEventViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.new_event), navController) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding)
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

            OutlinedTextField(
                value = state.location?.let { "${it.latitude}, ${it.longitude}" } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.meeting_point)) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.actions.onSelectChooseLocation() }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.open_map)
                        )
                    }
                },
                modifier = Modifier
                    .clickable { viewModel.actions.onSelectChooseLocation() }
            )


            Spacer(modifier = Modifier.height(24.dp))

//            Button(
//                onClick = { viewModel.actions.onCreateEvent() },
//                enabled = state.name.isNotBlank() && state.description.isNotBlank()
//            ) {
//                Text(stringResource(R.string.create_event))
//            }
        }

        if (state.showMapModal) {
            MapPickerBottomSheet(
                viewModel.actions.onDismissChooseLocation,
                viewModel.actions.onLocationChange
            )
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerBottomSheet(
    onDismiss: () -> Unit,
    onLocationSelected: (LatLng) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Occupa l'80% dello schermo
                .padding(16.dp)
        ) {
            Text("Seleziona un punto", style = MaterialTheme.typography.titleLarge)

            val cameraPositionState = rememberCameraPositionState()
            val markerState = rememberUpdatedMarkerState()

            GoogleMap(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                cameraPositionState = cameraPositionState,
                onMapClick = { markerState.position = it }
            ) {
                Marker(state = markerState)
            }

            Button(
                onClick = {
                    onLocationSelected(markerState.position)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Text("Conferma Posizione")
            }
        }
    }
}