package com.example.cacciaaltesoro.ui.screens.onlineevents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                for (event in viewModel.getState().ListEvent) {
                    HorizontalCard(event)
                }

                Spacer(modifier = Modifier.height(16.dp))


                }
            }
        }
    }


@Composable
fun ViewEvents(events: List<EventDTO>) {
    for (event in events) {
        Box(
            modifier = Modifier
                .requiredSize(381.dp, 80.dp)
                .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = event.name.orEmpty(),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
    }


}

@Composable
fun HorizontalCard(
    events: EventDTO
) {
    // Definizione colori dai tuoi hex
    val surfaceColor = Color(0xFFFEF7FF)
    val outlineVariant = Color(0xFFCAC4D0)
    val primaryContainer = Color(0xFFEADDFF)
    val onPrimaryContainer = Color(0xFF4F378A)
    val onSurface = Color(0xFF1D1B20)

    // Contenitore Principale (Card)
    Row(
        modifier = Modifier
            .width(381.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, outlineVariant, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Sezione Content (Avatar + Text)
        Row(
            modifier = Modifier
                .weight(1f) // flex-grow: 1
                .fillMaxHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = events.name!!,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        letterSpacing = 0.1.sp,
                        color = onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = events.name!!,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = onSurface
                    )
                )
                Text(
                    text = events.description!!,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = onSurface
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .border(
                    width = 1.dp,
                    color = outlineVariant,
                    // Il CSS specifica border-width: 1px 1px 1px 0px
                    // In Compose è più semplice gestirlo con un layout pulito
                )
                .background(Color(0xFFECE6F0)) // Background placeholder
        ) {/*
            if (mediaResId != null) {
                Image(
                    painter = painterResource(id = mediaResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }*/
        }
    }
}
