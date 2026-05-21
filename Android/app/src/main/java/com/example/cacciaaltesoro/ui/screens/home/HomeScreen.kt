package com.example.cacciaaltesoro.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel

@Composable
fun HomeScreen(navController: NavHostController, loginViewModel: LoginScreenViewModel) {

    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(
            title = stringResource(R.string.home),
            navController = navController,
            showProfile = true,
            imageUri = stateLogin.imageUri
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.home_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(72.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyButton(stringResource(R.string.event_explorer)) {
                    navController.navigate(NavigationRoute.OnlineEvents)
                }

                MyButton(stringResource(R.string.new_event)) {
                    if (stateLogin.isLogin)
                        navController.navigate(NavigationRoute.EventEditor())
                    else
                        navController.navigate(NavigationRoute.Login)
                }

                // TODO START DELETE BLOCK
                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                var gameID by remember { mutableStateOf("") }
                Text("DA ELIMINARE IN PRODUZIONE", color = MaterialTheme.colorScheme.error)
                OutlinedTextField(
                    value = gameID,
                    label = { Text("ID partita test") },
                    onValueChange = { gameID = it }
                )
                Button(
                    onClick = { navController.navigate(NavigationRoute.Game(gameID.toInt())) }
                ) {
                    Text("Partecipa")
                }
                // TODO END DELETE BLOCK
            }
        }
    }
}

@Composable
fun MyButton(label: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}