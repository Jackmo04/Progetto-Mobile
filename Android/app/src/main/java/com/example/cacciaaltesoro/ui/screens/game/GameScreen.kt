package com.example.cacciaaltesoro.ui.screens.game

import android.Manifest
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Tag
import com.example.cacciaaltesoro.data.mappers.toLatLng
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.utils.LocationService
import com.example.cacciaaltesoro.utils.nfc.NfcReaderLifecycle
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
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
fun GameScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val sheetContentState by viewModel.sheetContentState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val tagsToFind by viewModel.tagsToFind.collectAsStateWithLifecycle()

    NfcReaderLifecycle(
        isActive = gameState is GameState.Playing,
        onTagDiscovered = { nfcTag ->
            viewModel.nfcActions.onNfcTagDiscovered(nfcTag)
        }
    )

    BackHandler(enabled = gameState is GameState.Playing) {
        // In a real app, you might show a confirmation dialog here
        navController.navigateUp()
    }

    BackHandler(enabled = sheetContentState is SheetContentState.SingleTagView) {
        viewModel.viewTagList()
    }

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

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = gameState is GameState.Playing
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(gameState) {
        if (gameState is GameState.Playing || gameState is GameState.Finished) {
            sheetState.partialExpand()
        } else if (gameState is GameState.WaitingToStart || gameState is GameState.Loading) {
            sheetState.hide()
        }
    }

    LaunchedEffect(sheetContentState) {
        if (sheetContentState is SheetContentState.SingleTagView) {
            sheetState.expand()
        }
    }

    LaunchedEffect(Unit) { getLocationOrRequestPermission() }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = { AppBar(
            title = when (gameState) {
                is GameState.WaitingToStart, is GameState.Loading -> stringResource(R.string.waiting_for_start)
                else -> stringResource(R.string.good_luck)
            },
            navController
        ) },
        sheetPeekHeight = 160.dp,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            Crossfade(targetState = sheetContentState, label = "sheet_content") { state ->
                when (state) {
                    is SheetContentState.ViewingList -> {
                        TagList(
                            tags = tagsToFind,
                            onTagClick = { tag -> viewModel.viewSingleTag(tag) }
                        )
                    }
                    is SheetContentState.SingleTagView -> {
                        TagView(state.tag)
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

        Surface(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (gameState) {
                is GameState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GameState.WaitingToStart -> {
                    GameWaiting((gameState as GameState.WaitingToStart).countDownTime)
                }
                else -> {
                    GamePlaying(
                        viewModel = viewModel,
                        coordinates = coordinates,
                        tagsToFind = tagsToFind
                    )
                }
            }
            if (gameState is GameState.Finished) {
                GameFinishedAlert(onDismiss = { navController.navigateUp() })
            }
        }
    }
}

@Composable
fun TagList(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            ListItem(
                leadingContent = { Text("N°") },
                headlineContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Indizio")
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }

        items(tags) { tag ->
            ListItem(
                leadingContent = {
                    Text(
                        text = tag.number.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                headlineContent = {
                    Text(
                        text = tag.textHint ?: "Nessun indizio",
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                },
                trailingContent = {
                    Icon(Icons.Default.Visibility, contentDescription = "Mostra")
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


@Composable
fun TagView(
    tag: Tag
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tag #${tag.number}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(16.dp))
        Text(
            text = tag.textHint ?: "Nessun indizio disponibile per questo tag.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        // TODO add image
    }
}

@Composable
fun GamePlaying(
    viewModel: GameViewModel,
    coordinates: com.example.cacciaaltesoro.utils.Coordinates?,
    tagsToFind: List<Tag>
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(
                coordinates?.latitude ?: viewModel.event?.lat ?: 44.148,
                coordinates?.longitude ?: viewModel.event?.lon ?: 12.236
            ),
            18f
        )
    }

    LaunchedEffect(coordinates) {
        coordinates?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it.toLatLng(), 18f)
        }
    }

    GoogleMap(
        properties = MapProperties(
            mapType = MapType.SATELLITE,
            isMyLocationEnabled = coordinates != null
        ),
        onMapClick = { viewModel.viewTagList() },
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize(),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = coordinates != null
        )
    ) {
        viewModel.event?.let {
            Marker(
                MarkerState(position = it.location.toLatLng()),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                title = stringResource(R.string.meeting_point)
            )
        }
        tagsToFind.forEach { tag ->
            Marker(
                MarkerState(position = tag.coordinates.toLatLng()),
                title = tag.number.toString(),
                onClick = {
                    viewModel.viewSingleTag(tag)
                    true
                }
            )
        }
    }
}

@Composable
fun GameWaiting(timeRemaining: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "L'evento inizia tra:",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = timeRemaining,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun GameFinishedAlert(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Partita terminata!") },
        text = { Text("Tempo scaduto") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Vai ai risultati")
            }
        }
    )
}
