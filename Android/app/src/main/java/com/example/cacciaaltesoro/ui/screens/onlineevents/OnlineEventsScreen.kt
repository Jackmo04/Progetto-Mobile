package com.example.cacciaaltesoro.ui.screens.onlineevents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.data.repositories.OnlineEventRepository
import com.example.cacciaaltesoro.ui.composables.AppBar
import kotlin.compareTo


var title = "Eventi disponibili"

@Composable
fun OnlineEventsScreen(navController: NavHostController , viewModel: OnlineEventViewModel) {
    Scaffold(
        topBar = {
            AppBar(title, navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                //.background(Color(0xFF444444))
                .border(2.dp, Color(0x1AFFFFFF), RoundedCornerShape(2.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                var searchQuery by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .requiredSize(381.dp, 80.dp)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.requiredSize(250.dp, 56.dp),
                        placeholder = { Text("Cerca...", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.White
                        )
                    )

                    Button(
                        onClick = { /* Search action */ },
                        modifier = Modifier.requiredSize(97.dp, 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Cerca")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .requiredSize(381.dp, 80.dp)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {


                }
            }
        }
    }
}

@Composable
fun ViewEvents(events: List<EventDTO>) {

    for (event in events) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = event.name.orEmpty(),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }


}
