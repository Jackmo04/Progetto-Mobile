package com.example.cacciaaltesoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.ui.CacciaAlTesoroNavGraph
import com.example.cacciaaltesoro.ui.theme.CacciaAlTesoroTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val loginRepository: LoginRepository by inject ()
    //private val supabase: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CacciaAlTesoroTheme {
                val navController = rememberNavController()
                CacciaAlTesoroNavGraph(navController)
            }
        }}
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data

        // Controlliamo se è il nostro link di reset
        if (uri != null && uri.scheme == "caccia-al-tesoro" && uri.host == "reset-password") {

            // IL METODO INFALLIBILE: Estraiamo i token dall'URL
            val fragment = uri.fragment // Supabase usa il fragment (dopo il '#') per i token

            if (fragment != null && fragment.contains("access_token")) {
                // Trasformiamo la stringa "access_token=xyz&refresh_token=abc" in una mappa
                val params = fragment.split("&").associate {
                    val parts = it.split("=")
                    parts[0] to (if (parts.size > 1) parts[1] else "")
                }

                val accessToken = params["access_token"]
                val refreshToken = params["refresh_token"]

                if (accessToken != null && refreshToken != null) {
                    // Forziamo Supabase ad autenticarsi con questi token!
                    lifecycleScope.launch {
                        try {
                            loginRepository.supabase.auth.importAuthToken(accessToken, refreshToken)
                            Log.i("MainActivity", "Token importato con successo!")
                            // Ora che è autenticato, mostriamo la UI del cambio password
                            loginRepository.setPasswordUpdateRequested(true)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Errore durante l'importazione del token", e)
                        }
                    }
                    return // Finito!
                }
            }
        }
    }}
