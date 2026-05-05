package com.example.cacciaaltesoro.ui.screens.savedevents

import android.Manifest
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.LocationService
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.EventListCard
import com.example.cacciaaltesoro.ui.composables.OrderComboBox
import com.example.cacciaaltesoro.ui.screens.onlineevents.toastDistancePermission
import com.example.cacciaaltesoro.utils.EventOrderType
import com.example.cacciaaltesoro.utils.rememberMultiplePermissions
import kotlinx.coroutines.launch

@Composable
fun SavedEventsScreen(navController: NavHostController , viewModel: SavedEventsViewModel) {


    val ctx = LocalContext.current

    val locationService = remember { LocationService(ctx) }
    val coordinates by locationService.coordinates.collectAsStateWithLifecycle()
    var isAnyGranted by remember { mutableStateOf(false) }
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
        isAnyGranted = permissionResults.values.any { it.isGranted }
        if (isAnyGranted) {
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
            AppBar(stringResource(R.string.saved_event_title), navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .border(2.dp, Color(0x1AFFFFFF), RoundedCornerShape(2.dp))
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
                    horizontalArrangement = Arrangement.Center

                ) {

                    OrderComboBox(options = EventOrderType.entries.map { it.type }) { selected ->
                        viewModel.action.onOrderChanged(selected)
                        if(selected == EventOrderType.DISTANCE.type && !isAnyGranted){
                            toastDistancePermission(ctx)
                        }
                    }


                }
                if (viewModel.isLoading) {
                    CircularProgressIndicator()
                }
                else{
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(viewModel.getState().listEvent) { event ->
                            EventListCard(event , event.organizerUUID == viewModel.getState().uuid)
                        }
                    }
                }
            }
        }
    }


}
fun toastDistancePermission(ctx: Context) {
    Toast.makeText(
        ctx,
        "Location permission is needed to find nearby events.",
        Toast.LENGTH_LONG
    ).show()
}




