package com.example.cacciaaltesoro.ui.composables

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

enum class Size { Sm, Lg }

@Composable
fun ImageWithPlaceholder(uri: Uri?, size: Size) {
    if (uri != null) {
        AsyncImage(uri,
            "Profile picture",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(if (size == Size.Sm) 72.dp else 128.dp)
                .clip(CircleShape)
        )
    } else {
        Image(
            Icons.Outlined.Image,
            "Profile picture",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier
                .size(if (size == Size.Sm) 72.dp else 128.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(if (size == Size.Sm) 20.dp else 36.dp)
        )
    }
}
