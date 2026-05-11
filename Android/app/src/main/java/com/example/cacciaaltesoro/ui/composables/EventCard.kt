package com.example.cacciaaltesoro.ui.composables

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cacciaaltesoro.BuildConfig
import com.example.cacciaaltesoro.data.database.dto.EventDTO
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import io.github.jan.supabase.auth.api.AuthenticatedApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import androidx.core.net.toUri

@Composable
fun EventCard(
    event: EventDTO,
    viewModel: EventDetailsViewModel,
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.action.saveIdUser()
    }

    val isMineEvent: Boolean = state.userId == event.organizerUUID
    Log.i("CardEvent", state.userId + " Organizzatore: " + event.organizerUUID)
    val ctx = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var addressText by remember { mutableStateOf("Caricamento indirizzo...") }

    LaunchedEffect(event.lat, event.lon) {
        val address = withContext(Dispatchers.IO) {
            getAddressFromCoords(event.lat, event.lon)
        }
        addressText = address
    }


    val mapImageUrl = getImageUrl(event)

    val backgroundColor = if (isMineEvent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    fun shareDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val textToShare = shareTextBuilder(event ,addressText)
                withContext(Dispatchers.Main) {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Travel")
                    ctx.startActivity(shareIntent)
                }
            } catch (e: Exception) {
                Log.e("ShareError", "Errore durante la condivisione", e)
            }
        }
    }
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
                        text = "Code: ${event.code} • ${if (event.isPrivate) "Private" else "Public"}",
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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

                Text(
                    text = addressText,
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

                IconButton(onClick = { openInMaps(event, ctx) }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Apri in Google Maps",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { shareDetails() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Event",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }}
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {


            if (!isMineEvent) {

                        Button(
                            onClick = {
                                if (!state.imSubscribe){
                                    viewModel.action.joinToEvent()
                                }
                                else{
                                    viewModel.action.unscribeFromEvent()
                                }
                            },
                            enabled = !state.isLoadingSubscription,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(130.dp)
                        ) {
                            if (state.isLoadingSubscription) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (!state.imSubscribe) "Inscriviti" else "Disiscriviti")
                            }
                        }

                    Button(
                        onClick = { }
                    ) {
                        Text("Avvia gioco")
                    }
                } else {
                    Button(
                        onClick = { showDeleteDialog = true },
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

@OptIn(ExperimentalTime::class)
fun shareTextBuilder(event: EventDTO, resolvedAddress: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm", java.util.Locale.ITALY)
    val dateTime = java.time.Instant.ofEpochSecond(event.startTime.epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(formatter)

    return """
        *📍 NUOVA CACCIA AL TESORO!*
        
        Ciao! Sei stato invitato a partecipare a un nuovo evento. Ecco i dettagli:
        
        *🏆 Nome:* ${event.name}
        *📅 Data:* $dateTime
        *📍 Punto di ritrovo:* $resolvedAddress
        
        *Codice di accesso:* `${event.code}`
        
        ---
        
        *📲 Come partecipare:*
        Scarica l'app, inserisci il codice qui sopra e preparati a trovare tutti i Tag!
        
        *Mappa:* https://maps.google.com/?q=${event.lat},${event.lon}
    """.trimIndent()
}

fun getAddressFromCoords(lat: Double, lng: Double): String {
    val context = GeoApiContext.Builder()
        .apiKey(BuildConfig.MAPS_KEY)
        .build()

    val location = LatLng(lat, lng)

    return try {
        val results = GeocodingApi.reverseGeocode(context, location)
            .language("it")
            .await()

        if (results.isNotEmpty()) {
            results[0].formattedAddress
        } else {
            "Nessun risultato"
        }
    } catch (e: Exception) {
        "Errore API: ${e.message}"
    } finally {
        context.shutdown()
    }
}
fun openInMaps(event: EventDTO , ctx: Context) {
    try {
        val uri = "https://maps.google.com/?q=${event.lat},${event.lon}".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        ctx.startActivity(mapIntent)
    } catch (e: Exception) {
        Toast.makeText(ctx, "Impossibile aprire le mappe", Toast.LENGTH_SHORT).show()
    }
}
