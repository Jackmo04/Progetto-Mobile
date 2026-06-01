package com.example.cacciaaltesoro.ui.composables

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.data.domain.Event
import com.example.cacciaaltesoro.data.mappers.isAvailableTheEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EventListCard(
    event: Event,
    isMyEvent: Boolean,
    onClick: () -> Unit,
    location: Location? = null
) {
    val surfaceColor = if (isMyEvent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val outlineVariant = if( !event.isAvailableTheEvent())MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val loading = stringResource(R.string.loading)
    val context = LocalContext.current

    var addressText by remember { mutableStateOf(loading) }

    LaunchedEffect(event.lat, event.lon) {
        addressText = withContext(Dispatchers.IO) {
            getAddressFromCords(event.lat, event.lon, onlyCity = true , contextMain = context)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .border(1.dp, outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = addressText,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (location != null) getDistanceFromMe(event, location) else "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
        }}
    }
}

private fun getDistanceFromMe(event: Event, currentLocation: Location?): String {
    if (currentLocation == null) return ""
    val eventLocation = Location("").apply {
        latitude = event.lat
        longitude = event.lon
    }
    val distanceInMeters = currentLocation.distanceTo(eventLocation)
    return if (distanceInMeters < 1000) {
        "${distanceInMeters.toInt()} m"
    } else {
        "%.1f km".format(distanceInMeters / 1000)
    }
}