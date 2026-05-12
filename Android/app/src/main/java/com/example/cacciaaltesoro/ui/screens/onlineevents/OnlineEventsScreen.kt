package com.example.cacciaaltesoro.ui.screens.onlineevents

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import com.example.cacciaaltesoro.utils.EventOrderType
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch


@Composable
fun OnlineEventsScreen(navController: NavHostController , viewModel: OnlineEventViewModel ) {
    val ctx = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val list = state.listEvent

    LaunchedEffect(state.idEventCodeSearched) {
        state.idEventCodeSearched?.let { id ->
            viewModel.action.resetIdEventCodeSearched()
            navController.navigate(NavigationRoute.EventDetails(id))

        }
    }


    val locationService = remember { LocationService(ctx) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    fun getCurrentLocation() = scope.launch {
        try {
            locationService.getCurrentLocation()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { permissionResults ->
        val grantedNow = permissionResults.values.any { it.isGranted }
        if (grantedNow) {
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


    LaunchedEffect(Unit) {
        try {
            getLocationOrRequestPermission()
        }catch (e: Exception){
            Log.e("Position", e.toString())

        }
    }
    LaunchedEffect(coordinates) {
        coordinates?.let {
            viewModel.action.saveCurrentLocation(Location("custom_provider").apply {
                latitude = coordinates!!.latitude
                longitude = coordinates!!.longitude
            })
        }
    }

    Scaffold(
        topBar = {
            AppBar(stringResource(id = R.string.online_event_title), navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {


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
                        placeholder = { Text(stringResource(R.string.event_code), color = MaterialTheme.colorScheme.primary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.secondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                            cursorColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    Button(
                        onClick = {
                           viewModel.action.saveIdEventCodeSearched(searchQuery)},
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(stringResource(R.string.find))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center

                ) {

                    OrderComboBox(options = EventOrderType.entries.map { it.type }) { selected ->
                        viewModel.action.onOrderChanged(selected)

                        val hasPermission = locationPermissions.statuses.any { it.value.isGranted }

                        if (selected == EventOrderType.DISTANCE.type && !hasPermission) {
                            toastDistancePermission(ctx)
                        }
                    }


                }
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    state = (rememberLazyListState())
                ) {
                    items(list) { event ->
                        EventListCard(event, event.organizerUUID == state.uuid) {
                            Log.i("CardLog" , event.id.toString())
                            event.id.let { id ->
                                navController.navigate(NavigationRoute.EventDetails(id))
                            }
                        }
                }
            }
            }
            }
        }


}}

fun toastDistancePermission(ctx: Context){
    Toast.makeText(
        ctx,
        "Location permission is needed to find nearby events.",
        Toast.LENGTH_LONG
    ).show()
}





