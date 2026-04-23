package com.example.cacciaaltesoro.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.ui.composables.AppBar

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Home", navController) }
    ) { innerPadding ->
        Text(
            text = "Home",
            modifier = Modifier.padding(innerPadding)
        )
    }
}