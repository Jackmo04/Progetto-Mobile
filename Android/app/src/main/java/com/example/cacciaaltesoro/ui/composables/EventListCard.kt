package com.example.cacciaaltesoro.ui.composables

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cacciaaltesoro.data.database.dto.EventDTO

@Composable
fun EventListCard(
    events: EventDTO,
    isMyEvent: Boolean
) {
    // Definizione colori dai tuoi hex
    val surfaceColor = if (isMyEvent) Color(0xFFEADDFF) else Color(0xFFFEF7FF)
    val outlineVariant = Color(0xFFCAC4D0)
    val primaryContainer = Color(0xFFEADDFF)
    val onPrimaryContainer = Color(0xFF4F378A)
    val onSurface = Color(0xFF1D1B20)

    // Contenitore Principale (Card)
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = events.name?.take(1)?.uppercase() ?: "?",
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
                    text = events.name ?: "Senza nome",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = onSurface
                    )
                )
                Text(
                    text = events.description ?: "Nessuna descrizione",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

    }
}