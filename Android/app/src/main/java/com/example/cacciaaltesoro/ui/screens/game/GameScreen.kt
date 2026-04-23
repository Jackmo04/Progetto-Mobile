package com.example.cacciaaltesoro.ui.screens.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar

@Composable
fun GameScreen(navController: NavHostController, eventId: String) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Game", navController) }
    ) { innerPadding ->
        Text(
            modifier = Modifier.padding(innerPadding),
            text = "Game"
        )
    }
}