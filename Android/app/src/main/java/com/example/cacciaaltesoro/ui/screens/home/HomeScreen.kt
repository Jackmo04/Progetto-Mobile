package com.example.cacciaaltesoro.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.NavigationRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel

@Composable
fun HomeScreen(navController: NavHostController,loginViewModel: LoginScreenViewModel ) {

    val stateLogin by loginViewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar(stringResource(R.string.home), navController,true,stateLogin.imageUri) }
    ) { innerPadding ->
        var eventId by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
             //   MyButton("Login") { navController.navigate(NavigationRoute.Login) }
                MyButton("Online Events") { navController.navigate(NavigationRoute.OnlineEvents) }
                if(stateLogin.isLogin){
                    MyButton("Saved Events") { navController.navigate(NavigationRoute.SavedEvents) }
                    MyButton("New Event") { navController.navigate(NavigationRoute.EventEditor()) }

                    // TODO delete following lines
                    Spacer(modifier = Modifier.height(24.dp))
                    MyButton("Test Game (id:33)") { navController.navigate(NavigationRoute.Game(33)) }
                }
            }
        }
    }
}

@Composable
fun MyButton(label: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.requiredSize(200.dp, 50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        onClick = onClick
    ) {
        Text(label)
    }
}