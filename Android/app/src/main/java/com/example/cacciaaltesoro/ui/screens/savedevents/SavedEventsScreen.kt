package com.example.cacciaaltesoro.ui.screens.savedevents

import android.Manifest
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.utils.LocationService
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.EventListCard
import com.example.cacciaaltesoro.ui.composables.OrderComboBox
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.utils.EventOrderType
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch

@Composable
fun SavedEventsScreen(navController: NavHostController, viewModel: SavedEventsViewModel,loginViewModel: LoginScreenViewModel) {
    val ctx = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationService = remember { LocationService(ctx) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    fun getCurrentLocation() = scope.launch {
        try {
            locationService.getCurrentLocation()
        } catch (e: SecurityException) {
            Log.e("SavedEvent", "Permesso negato", e)
        } catch (e: Exception) {
            Log.e("SavedEvent", "Errore location", e)
        }
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { permissionResults ->
        if (permissionResults.values.any { it.isGranted }) {
            getCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.action.onSearchEvent()
        if (locationPermissions.statuses.any { it.value.isGranted }) {
            getCurrentLocation()
        } else {
            locationPermissions.launchPermissionRequest()
        }
    }

    LaunchedEffect(coordinates) {
        coordinates?.let {
            viewModel.action.saveCurrentLocation(Location("custom_provider").apply {
                latitude = it.latitude
                longitude = it.longitude
            })
        }
    }

    Scaffold(
        topBar = { AppBar(stringResource(R.string.saved_event_title), navController,true,stateLogin.imageUri) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                OrderComboBox(options = EventOrderType.entries.map { it.type }) { selected ->
                    viewModel.action.onOrderChanged(selected)
                    val hasPermission = locationPermissions.statuses.any { it.value.isGranted }

                    if (selected == EventOrderType.DISTANCE.type && !hasPermission) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Permesso di posizione necessario per trovare eventi vicini.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.listEvent.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Nessun evento salvato",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Non hai salvato nessun evento",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = state.listEvent,
                        key = { event -> event.id ?: event.hashCode() }
                    ) { event ->
                        EventListCard(
                            events = event,
                            isMyEvent = event.organizerUUID == state.uuid,
                            onClick = {
                                event.id?.let { id ->
                                    navController.navigate(NavigationRoute.EventDetails(id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}