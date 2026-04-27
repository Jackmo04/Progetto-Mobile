package com.example.cacciaaltesoro.ui.screens.login

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.ui.CacciaAlTesoroRoute
import com.example.cacciaaltesoro.ui.composables.AppBar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.jvm.java

@Composable
fun LoginScreen(
    onSignUp: (String, String) -> Unit,
    onLogIn: (String, String) -> Unit,
    navController: NavHostController,
    isSignUp : Boolean
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val title = if (isSignUp)   "Accedi" else "Registrati"

    Scaffold(
        topBar = {
            AppBar(title, navController)
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username= it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(36.dp))
            if (isSignUp) {
                MyButton("Accedi" , onClick ={ onLogIn(username , password) })
                Spacer(modifier = Modifier.size(36.dp))
                LoginAnswer(navController)
            }else{
                MyButton("Registrati" , onClick ={ onSignUp(username , password) })
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

@Composable
fun LoginAnswer( navController: NavController) {

    val annotatedText = buildAnnotatedString {
        append("Non sei registrato? ")


        val clickableLink = LinkAnnotation.Clickable(
            tag = "toggle_mode",
            styles = TextLinkStyles(
                style = SpanStyle(
                    color = Color.Blue
                )
            )
        ) {
            navController.navigate(CacciaAlTesoroRoute.SignUp)

        }

        withLink(clickableLink) {
            append("Registrati")
        }

        append(".")
    }
    Text(text = annotatedText)

}

