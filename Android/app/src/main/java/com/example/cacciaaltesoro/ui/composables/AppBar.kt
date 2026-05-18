package com.example.cacciaaltesoro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.NavigationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    navController: NavHostController,
    showBackArrow: Boolean = true,
    showProfile: Boolean = false,
    imageUri: Any? = null,
    onProfileClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Medium
            )
        },
        navigationIcon = {
            if (showBackArrow && navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        "Go Back"
                    )
                }
            }
        },
        actions = {
            if (showProfile) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    AsyncImage(
                        model = imageUri,
                        placeholder = painterResource(R.drawable.profilo_default),
                        fallback = painterResource(R.drawable.profilo_default),
                        error = painterResource(R.drawable.profilo_default),
                        contentDescription = "Foto del profilo",
                        modifier = Modifier
                            .size(36.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = {navController.navigate(NavigationRoute.Login)}),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}