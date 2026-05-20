package com.example.cacciaaltesoro.ui.composables

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cacciaaltesoro.BuildConfig
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import androidx.core.net.toUri
import com.example.cacciaaltesoro.data.domain.Event
import java.time.Instant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.cacciaaltesoro.R
import com.google.maps.model.AddressComponentType
import kotlin.time.Clock

@OptIn(ExperimentalTime::class)
@Composable
fun EventCard(
    event: Event,
    viewModel: EventDetailsViewModel,
    navController: NavHostController
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.action.saveIdUser()
    }

    val isMineEvent: Boolean = state.userId == event.organizerUUID
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val loading = stringResource(R.string.loading)
    var addressText by remember { mutableStateOf(loading) }

    LaunchedEffect(event.lat, event.lon) {
        val address = withContext(Dispatchers.IO) {
            getAddressFromCords(event.lat, event.lon)
        }
        addressText = address
    }

    val mapImageUrl = getImageUrl(event)
    val backgroundColor = if (isMineEvent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    fun shareDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val textToShare = shareTextBuilder(event, addressText)
                withContext(Dispatchers.Main) {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                    }
                    val shareIntent = Intent.createChooser(sendIntent,
                        context.getString(R.string.share_event))
                    context.startActivity(shareIntent)
                }
            } catch (e: Exception) {
                Log.e("ShareError", "Errore durante la condivisione", e)
            }
        }
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMineEvent) MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.2f
                            ) else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = "${stringResource(R.string.code)} ${event.code} • ${if (event.isPrivate) stringResource(R.string.private_k) else stringResource(R.string.public_k)}",
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
                contentDescription = "${stringResource(R.string.map_position_for)} ${event.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { openInMaps(event, context) },
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!event.description.isNullOrBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                InfoRow(icon = Icons.Default.Person, text = "${stringResource(R.string.master)} ${event.organizer?.username ?: stringResource(
                    R.string.unknow)}")
                InfoRow(icon = Icons.Default.LocationOn, text = "${stringResource(R.string.Address)} $addressText")
                InfoRow(icon = Icons.Default.AccessTime, text = "${stringResource(R.string.start_event)} ${getStartTime(event)}")
                InfoRow(icon = Icons.Default.Timer, text = "${stringResource(R.string.duration)} ${getGameDuration(event)}")
                if(!isMineEvent && state.imSubscribe)
                    InfoRow(icon = Icons.Default.Tag, text = "${stringResource(R.string.tag_catched)} ${state.userTagCached}")
                else
                    InfoRow(icon = Icons.Default.PersonAdd, text = "${stringResource(R.string.subscribed)} ${state.registeredUser}")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { addToCalendar(event, addressText, context) },
                    enabled = state.imSubscribe
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.add_to_calendar),
                        tint = if (state.imSubscribe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                IconButton(onClick = { shareDetails() }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_event),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!isMineEvent) {
                    OutlinedButton(
                        onClick = {
                            if (!state.imSubscribe) viewModel.action.joinToEvent()
                            else viewModel.action.unscribeFromEvent()
                        },
                        enabled = !state.isLoadingSubscription,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (state.isLoadingSubscription) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (!state.imSubscribe) stringResource(R.string.subscribe) else stringResource(
                                R.string.unscribe
                            ))
                        }
                    }

                    Button(
                        // TODO quando event id sarà UUID, togliere elvis
                        onClick = { navController.navigate(NavigationRoute.Game(
                            event.id ?: throw IllegalArgumentException())
                        ) },
                        enabled = state.imSubscribe && isAvailableTheEvent(event)
                    ) {
                        Text(stringResource(R.string.start))
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }

                    Button(
                        onClick = { navController.navigate(NavigationRoute.EventEditor(eventId = event.id)) },
                        enabled = isEditableTheEvent(event)
                    ) {
                        Text("Modifica")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Conferma eliminazione") },
            text = { Text(text = "Sei sicuro di voler cancellare questo evento? Questa azione non può essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.action.deleteEvent()
                        showDeleteDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annulla") }
            }
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getImageUrl(event: Event) : String{
   return "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=${event.lat},${event.lon}" +
            "&zoom=15" +
            "&size=600x300" +
            "&markers=color:red%7C${event.lat},${event.lon}" +
            "&key=${BuildConfig.MAPS_KEY}"
}

@OptIn(ExperimentalTime::class)
fun shareTextBuilder(event: Event, resolvedAddress: String): String {
    val dateTime = getStartTime(event)
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

fun getAddressFromCords(lat: Double, lng: Double, onlyCity: Boolean = false): String {
    val context = GeoApiContext.Builder()
        .apiKey(BuildConfig.MAPS_KEY)
        .build()

    return try {
        val results = GeocodingApi.reverseGeocode(context, LatLng(lat, lng))
            .language("it")
            .await()

        if (results.isNotEmpty()) {
            if (onlyCity) {
                val cityComponent = results[0].addressComponents.find { component ->
                    component.types.contains(AddressComponentType.LOCALITY)
                } ?: results[0].addressComponents.find { component ->
                    component.types.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_3)
                }

                cityComponent?.longName ?: "Città non trovata"
            } else {
                results[0].formattedAddress
            }
        } else {
            "Nessun risultato"
        }
    } catch (e: Exception) {
        "Errore API: ${e.message}"
    } finally {
        context.shutdown()
    }
}
fun openInMaps(event: Event , ctx: Context) {
    try {
        val uri = "https://maps.google.com/?q=${event.lat},${event.lon}".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        ctx.startActivity(mapIntent)
    } catch (e: Exception) {
        Toast.makeText(ctx, "Impossibile aprire le mappe", Toast.LENGTH_SHORT).show()
    }
}


@OptIn(ExperimentalTime::class)
fun addToCalendar(event: Event, address: String, ctx: Context) {
    try {
        val startTimeMillis = event.startTime.epochSeconds * 1000L
        val endTimeMillis = event.endTime.epochSeconds * 1000L

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
            putExtra(CalendarContract.Events.TITLE, event.name)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description ?: "Caccia al Tesoro!")
            putExtra(CalendarContract.Events.EVENT_LOCATION, address)
        }

        ctx.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(ctx, "Nessuna app calendario trovata", Toast.LENGTH_SHORT).show()
        Log.e("CalendarError", "Errore durante l'apertura del calendario", e)
    }
}

@OptIn(ExperimentalTime::class)
fun getStartTime(event: Event): String{
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm", java.util.Locale.ITALY)
    val dateTime = Instant.ofEpochSecond(event.startTime.epochSeconds)
        .atZone(ZoneId.systemDefault())
        .format(formatter)

    return dateTime
}

@OptIn(ExperimentalTime::class)
fun getGameDuration(event: Event): String {
    val diffInSeconds = event.endTime.epochSeconds - event.startTime.epochSeconds
    val minutes = diffInSeconds / 60

    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes > 0) "$hours h e $remainingMinutes min" else "$hours h"
    } else {
        "$minutes minuti"
    }
}

@OptIn(ExperimentalTime::class)
fun isAvailableTheEvent(event: Event): Boolean {
    val now = Clock.System.now().toEpochMilliseconds()
    val extratime = 15 * 60 * 1000
    return (event.startTime.epochSeconds * 1000L - now - extratime) <= 0 && (event.endTime.epochSeconds *1000L - now)>=0
}

@OptIn(ExperimentalTime::class)
fun isEditableTheEvent(event: Event): Boolean {
    val now = Clock.System.now().toEpochMilliseconds()
    return (event.startTime.epochSeconds * 1000L) > now
}