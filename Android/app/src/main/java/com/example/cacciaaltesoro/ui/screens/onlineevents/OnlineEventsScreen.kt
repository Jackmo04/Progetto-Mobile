package com.example.cacciaaltesoro.ui.screens.onlineevents

import android.Manifest
import android.location.Location
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineEventsScreen(
    navController: NavHostController,
    viewModel: OnlineEventsViewModel,
    loginViewModel: LoginScreenViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()
    val list = state.listEvent

    val permissionDeniedMessage = stringResource(R.string.position_permission_required)
    var isPullRefreshing by remember { mutableStateOf(false) }

    val distanceString = stringResource(EventOrderType.DISTANCE.stringResId)
    val orderOptionsMap = EventOrderType.entries.associateBy { stringResource(it.stringResId) }
    val optionsList = orderOptionsMap.keys.toList()

    val locationService = remember { LocationService(context) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()

    val errorStringRes = viewModel.errorMessage
    val errorString = errorStringRes?.let { stringResource(it) }

    LaunchedEffect(state.idEventCodeSearched) {
        state.idEventCodeSearched?.let { id ->
            viewModel.action.resetIdEventCodeSearched()
            navController.navigate(NavigationRoute.EventDetails(id))
        }
    }

    LaunchedEffect(state.orderString) {
        viewModel.action.loadEvents(state.currentFilter)
    }

    LaunchedEffect(errorString) {
        if (errorString != null) {
            snackbarHostState.showSnackbar(errorString)
            viewModel.action.clearErrorMessage()
        }
    }


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
                snackbarHostState.showSnackbar(permissionDeniedMessage)
            }
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

    LaunchedEffect(Unit) {
        if (locationPermissions.statuses.any { it.value.isGranted }) {
            getCurrentLocation()
        }
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isPullRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.event_explorer),
                navController = navController,
                showProfile = true,
                imageUri = stateLogin.imageUri,
                imLogged = stateLogin.isLogin
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = state.currentFilter.ordinal,
                containerColor = TabRowDefaults.primaryContainerColor,
                contentColor = TabRowDefaults.primaryContentColor,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(state.currentFilter.ordinal, matchContentSize = true)
                    )
                },
                divider = { HorizontalDivider() }
            ) {
                Tab(
                    selected = state.currentFilter == EventFilterType.ONLINE,
                    onClick = {
                        viewModel.action.loadEvents(EventFilterType.ONLINE)
                    },
                    text = { Text(stringResource(R.string.online)) }
                )
                if (stateLogin.isLogin) {
                    Tab(
                        selected = state.currentFilter == EventFilterType.SAVED,
                        onClick = {
                            viewModel.action.loadEvents(EventFilterType.SAVED)
                        },
                        text = { Text(stringResource(R.string.saved)) }
                    )
                    Tab(
                        selected = state.currentFilter == EventFilterType.CREATED,
                        onClick = {
                            viewModel.action.loadEvents(EventFilterType.CREATED)
                        },
                        text = { Text(stringResource(R.string.created)) }
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                if (state.currentFilter == EventFilterType.ONLINE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OrderComboBox(options = optionsList) { selected ->
                        val orderType = orderOptionsMap[selected]

                        if (selected == distanceString) {
                            if (locationPermissions.statuses.any { it.value.isGranted }) {
                                getCurrentLocation()
                                orderType?.let { viewModel.action.onOrderChanged(it.name) }
                            } else {
                                locationPermissions.launchPermissionRequest()
                            }
                        } else {
                            orderType?.let { viewModel.action.onOrderChanged(it.name) }
                        }
                    }
                }

                if (viewModel.isLoading && list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = isPullRefreshing,
                        onRefresh = {
                            isPullRefreshing = true
                            viewModel.action.loadEvents(state.currentFilter)
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (list.isEmpty()) {
                            val icon = if (state.currentFilter == EventFilterType.ONLINE) Icons.Default.EventBusy else Icons.Default.BookmarkBorder
                            val textEmpty = stringResource(R.string.no_event_found)

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
                                        event = event,
                                        isMyEvent = event.organizerUUID == state.uuid,
                                        onClick = {
                                            event.id?.let { id -> navController.navigate(NavigationRoute.EventDetails(id))} },
                                        viewModel.currentLocation
                                    )
                                }
                            }
                        }
                    }
            }}
        }
    }
}
