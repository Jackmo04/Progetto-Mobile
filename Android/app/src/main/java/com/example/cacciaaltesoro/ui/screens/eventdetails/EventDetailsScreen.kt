package com.example.cacciaaltesoro.ui.screens.eventdetails

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.EventCard
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EventDetailsScreen(navController: NavHostController,
                       eventId: Int,
                       viewModel: EventDetailsViewModel = koinViewModel(),
    loginViewModel: LoginScreenViewModel) {
    val state by viewModel.state.collectAsState()
    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect (eventId) {
        Log.i("CardLog" , eventId.toString())
        viewModel.action.findEventByID(eventId)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.event_details), navController,true,stateLogin.imageUri) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
        ) {
            if (state.event != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EventCard(state.event!!, viewModel, navController)
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
