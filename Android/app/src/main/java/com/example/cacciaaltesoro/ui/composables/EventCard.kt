package com.example.cacciaaltesoro.ui.composables

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cacciaaltesoro.BuildConfig
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel

@Composable
fun EventCard(
    event: EventDTO,
    viewModel: EventDetailsViewModel,
    navController: NavHostController
) {

    viewModel.action.saveIdUser()
    val isMineEvent : Boolean = viewModel.getState().userId == event.organizerUUID

    Log.i("CardEvent", viewModel.getState().userId +" pippo "+ event.organizerUUID)
    val ctx = LocalContext.current

    val imSubscribe = viewModel.getState().imSubscribe
    var showDeleteDialog by remember { mutableStateOf(false) }



    val mapImageUrl = getImageUrl(event)

    val backgroundColor = if (isMineEvent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    OutlinedCard(
        modifier = Modifier
            .width(412.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backgroundColor), // M3/sys/light/primary-container
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Code: ${event.code} • ${if(event.isPrivate) "Private" else "Public"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mapImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Map location for ${event.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Placeholder background color
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Organizer: ${event.userDTO?.username ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = event.description ?: "No description provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Handle more action */ }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "More options",
                        tint = Color(0xFF49454F)
                    )
                }

                if (!isMineEvent){
                    if(!imSubscribe){

                Button(
                    onClick = { viewModel.action.joinToEvent() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Iscriviti")
                }}
                    else{
                        Button(
                            onClick = { viewModel.action.unscribeFromEvent() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Disiscriviti")
                        }
                    }

                Button(
                    onClick = {  }
                ) {
                    Text("Avvia gioco")
                }}
                else{
                    Button(
                        onClick = {showDeleteDialog = true  },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xC8B24843))
                    ) {
                        Text("Cancella")
                    }
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDeleteDialog = false
                            },
                            title = {
                                Text(text = "Conferma eliminazione")
                            },
                            text = {
                                Text(text = "Sei sicuro di voler cancellare questo evento? Questa azione non può essere annullata.")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.action.deleteEvent()
                                        showDeleteDialog = false
                                        navController.navigateUp()

                                    }
                                ) {
                                    Text("OK", color = Color(0xC8B24843))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                    }
                                ) {
                                    Text("Annulla")
                                }
                            }
                        )
                    }
                    Button(
                        onClick = { navController.navigate(NavigationRoute.EventEditor(eventId = event.id)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xC8B24843))
                    ) {
                        Text("Modifica")
                    }
                }
            }
        }
    }


}

fun getImageUrl(event: EventDTO) : String{
   return "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=${event.lat},${event.lon}" +
            "&zoom=15" +
            "&size=600x300" +
            "&markers=color:red%7C${event.lat},${event.lon}" +
            "&key=${BuildConfig.MAPS_KEY}"
}
