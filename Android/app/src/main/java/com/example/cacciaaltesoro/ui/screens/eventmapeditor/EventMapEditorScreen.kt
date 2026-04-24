package com.example.cacciaaltesoro.ui.screens.eventmapeditor

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun EventMapEditorScreen(navController: NavHostController, eventId: String) {
    val selectedCoordinates = remember { mutableStateListOf<LatLng>() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.new_event), navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapMarkerEditor(
                currentCoordinates = selectedCoordinates
            ) { coordinates ->
                // TODO send to viewModel
                selectedCoordinates.clear()
                selectedCoordinates.addAll(coordinates)
            }
        }
    }
}

@Composable
fun MapMarkerEditor(currentCoordinates: List<LatLng>, onExportCoordinates: (List<LatLng>) -> Unit) {
    val markers = remember {
        mutableStateListOf<LatLng>().apply {
            addAll(currentCoordinates)
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 10f)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markers.add(latLng)
            }
        ) {
            markers.forEach { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "Marker ${markers.indexOf(position) + 1}",
                    onInfoWindowClick = {
                        markers.remove(position)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp),
            horizontalArrangement = SpaceEvenly
        ) {
            Button(
                onClick = { onExportCoordinates(markers.toList()) },
                enabled = markers.isNotEmpty()
            ) {
                Text("Export Coordinates")
            }

            Button(
                onClick = {
                    markers.clear()
                    onExportCoordinates(emptyList())
                },
                enabled = markers.isNotEmpty()
            ) {
                Text("Clear Markers")
            }
        }
    }
}