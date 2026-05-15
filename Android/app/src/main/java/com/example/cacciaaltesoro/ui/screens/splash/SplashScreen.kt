package com.example.cacciaaltesoro.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.ui.NavigationRoute
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    navController: NavHostController,
    // Usiamo Koin per iniettare direttamente Supabase (o passalo dai parametri se non usi Koin qui)
    supabase: SupabaseClient = koinInject()
) {
    // Ascoltiamo lo stato IN TEMPO REALE di Supabase
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    // Reagiamo ai cambiamenti di stato
    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                // Utente loggato! Vai alla Home (o agli Eventi) e DISTRUGGI la Splash dalla cronologia
                navController.navigate(NavigationRoute.Home) { // Sostituisci con la tua rotta
                    popUpTo(NavigationRoute.Splash) { inclusive = true }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                // Non loggato o errore! Vai al Login e DISTRUGGI la Splash dalla cronologia
                navController.navigate(NavigationRoute.Login) {
                    popUpTo(NavigationRoute.Splash) { inclusive = true }
                }
            }
            else -> {
                // SessionStatus.LoadingFromStorage -> Stiamo ancora caricando, non fare nulla (mostra la UI)
            }
        }
    }

    // --- UI della Splash Screen ---
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Sostituisci con il logo della tua app!
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Logo Caccia al Tesoro",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}