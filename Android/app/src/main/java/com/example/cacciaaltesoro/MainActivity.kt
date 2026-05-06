package com.example.cacciaaltesoro

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.ui.CacciaAlTesoroNavGraph
import com.example.cacciaaltesoro.ui.theme.CacciaAlTesoroTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val loginRepository: LoginRepository by inject ()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CacciaAlTesoroTheme {
                val navController = rememberNavController()
                CacciaAlTesoroNavGraph(navController)
            }
        }
    }
    private fun handleIntent(intent: Intent) {
        val uri = intent.data

        if (uri != null && uri.host == "reset-password") {

            loginRepository.setPasswordUpdateRequested(true)

        }
    }
}
