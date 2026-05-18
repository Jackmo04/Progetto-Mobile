package com.example.cacciaaltesoro.ui.screens.onlineevents // Usa il tuo package

import android.Manifest
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.EventListCard
import com.example.cacciaaltesoro.ui.composables.OrderComboBox
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.utils.EventOrderType
import com.example.cacciaaltesoro.utils.LocationService
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch

@Composable
fun OnlineEventsScreen(
    navController: NavHostController,
    viewModel: OnlineEventsViewModel,
    loginViewModel: LoginScreenViewModel
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()
    val list = state.listEvent

    LaunchedEffect(state.idEventCodeSearched) {
        state.idEventCodeSearched?.let { id ->
            viewModel.action.resetIdEventCodeSearched()
            navController.navigate(NavigationRoute.EventDetails(id))
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.action.clearErrorMessage()
        }
    }

    val locationService = remember { LocationService(ctx) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()

    fun getCurrentLocation() = scope.launch {
        try {
            locationService.getCurrentLocation()
        } catch (e: Exception) {
            Log.e("Position", "Errore location", e)
        }
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { permissionResults ->
        if (permissionResults.values.any { it.isGranted }) {
            getCurrentLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permesso di posizione necessario.")
            }
        }
    }

    LaunchedEffect(coordinates) {
        coordinates?.let {
            viewModel.action.saveCurrentLocation(Location("custom_provider").apply {
                latitude = it.latitude
                longitude = it.longitude
            })
            viewModel.action.onOrderChanged(EventOrderType.DISTANCE.type)
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = if (state.currentFilter == EventFilterType.ONLINE) "Esplora Eventi" else "I Miei Eventi",
                navController = navController,
                showProfile = true,
                imageUri = stateLogin.imageUri
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = state.currentFilter.ordinal) {
                Tab(
                    selected = state.currentFilter == EventFilterType.ONLINE,
                    onClick = { viewModel.action.loadEvents(EventFilterType.ONLINE) },
                    text = { Text("Tutti") }
                )
                if (stateLogin.isLogin) {
                    Tab(
                        selected = state.currentFilter == EventFilterType.SAVED,
                        onClick = { viewModel.action.loadEvents(EventFilterType.SAVED) },
                        text = { Text("Salvati") }
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                if (state.currentFilter == EventFilterType.ONLINE) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.event_code)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                    viewModel.action.saveIdEventCodeSearched(searchQuery)
                                }
                            )
                        )
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.action.saveIdEventCodeSearched(searchQuery)
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.find))
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OrderComboBox(options = EventOrderType.entries.map { it.type }) { selected ->
                        if (selected == EventOrderType.DISTANCE.type) {
                            if (locationPermissions.statuses.any { it.value.isGranted }) {
                                getCurrentLocation()
                                viewModel.action.onOrderChanged(selected)
                            } else {
                                locationPermissions.launchPermissionRequest()
                            }
                        } else {
                            viewModel.action.onOrderChanged(selected)
                        }
                    }
                }

                if (viewModel.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (list.isEmpty()) {
                    val icon = if (state.currentFilter == EventFilterType.ONLINE) Icons.Default.EventBusy else Icons.Default.BookmarkBorder
                    val textEmpty = if (state.currentFilter == EventFilterType.ONLINE) "Nessun evento trovato" else "Non hai salvato nessun evento"

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(textEmpty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = list, key = { event -> event.id!! }) { event ->
                            EventListCard(
                                events = event,
                                isMyEvent = event.organizerUUID == state.uuid,
                                onClick = { event.id?.let { id -> navController.navigate(NavigationRoute.EventDetails(id)) } }
                            )
                        }
                    }
                }
            }
        }
    }
}