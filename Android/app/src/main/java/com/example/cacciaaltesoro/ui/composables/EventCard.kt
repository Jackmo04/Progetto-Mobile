package com.example.cacciaaltesoro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cacciaaltesoro.BuildConfig
import com.example.cacciaaltesoro.data.database.dto.EventDTO

@Composable
fun EventCard(
    event: EventDTO
) {
    val mapImageUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
            "center=${event.lat},${event.lon}" +
            "&zoom=15" +
            "&size=600x300" +
            "&markers=color:red%7C${event.lat},${event.lon}" +
            "&key=${BuildConfig.MAPS_KEY}"

    OutlinedCard(
        modifier = Modifier
            .width(412.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFFEF7FF)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCAC4D0)) // M3/sys/light/outline-variant
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
                        .background(Color(0xFFEADDFF)), // M3/sys/light/primary-container
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4F378A)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name ?: "Unknown Event",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1D1B20),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Code: ${event.code} • ${if(event.isPrivate) "Private" else "Public"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1D1B20),
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
                    .background(Color(0xFFECE6F0)), // Placeholder background color
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = event.name ?: "Event Details",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = Color(0xFF1D1B20),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Organizer: ${event.userDTO?.username ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = event.description ?: "No description provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF49454F),
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

                Button(
                    onClick = {  },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Iscriviti")
                }

                Button(
                    onClick = {  }
                ) {
                    Text("Avvia gioco")
                }
            }
        }
    }
}