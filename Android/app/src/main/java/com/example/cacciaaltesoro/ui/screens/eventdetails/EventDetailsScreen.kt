package com.example.cacciaaltesoro.ui.screens.eventdetails

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.composables.EventCard
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EventDetailsScreen(navController: NavHostController,
                       eventId: Int,
                       viewModel: EventDetailsViewModel = koinViewModel()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.event_details), navController) }
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
        viewModel.action.findEventByID(eventId)
        EventCard(viewModel.getState().event!!)
    }
}}}