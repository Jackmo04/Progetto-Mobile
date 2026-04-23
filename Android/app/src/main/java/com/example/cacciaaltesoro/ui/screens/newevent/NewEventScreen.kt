package com.example.cacciaaltesoro.ui.screens.newevent

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
fun NewEventScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.new_event), navController) }
    ) { innerPadding ->
        Text(
            modifier = Modifier.padding(innerPadding),
            text = "New event"
        )
    }
}