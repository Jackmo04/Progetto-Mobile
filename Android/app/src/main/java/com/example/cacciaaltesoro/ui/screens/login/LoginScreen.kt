package com.example.cacciaaltesoro.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = koinViewModel()
) {
    var username by remember { mutableStateOf(viewModel.getState().username) }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val isSignUp = viewModel.getState().isSignUp
    val isUpdatePassword = viewModel.getState().isUpdatePassword

    LaunchedEffect(viewModel.getState().username) {
        if (username.isEmpty()) {
            username = viewModel.getState().username
        }
    }

    val title = if (viewModel.getState().isLogin) {
        if (isUpdatePassword) "Aggiorna Password" else stringResource(R.string.profile_title)
    } else if (isUpdatePassword) {
        "Aggiorna Password"
    } else if (!isSignUp) {
        stringResource(R.string.login_title)
    } else {
        stringResource(R.string.signup_title)
    }

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

            if (!isUpdatePassword) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { text -> username = text },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading,
                    readOnly = viewModel.getState().isLogin
                )

                if (!viewModel.getState().isLogin) {
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading,
                    )
                }
            } else {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nuova Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading,
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text(stringResource(R.string.password_confirm)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                if (isUpdatePassword) {
                    MyButton("Aggiorna Password", onClick = {
                        viewModel.action.changePassword(username, password, passwordConfirm)
                    })
                    Spacer(modifier = Modifier.size(8.dp))
                    MyButton("Annulla", onClick = { viewModel.action.toggleUpdatePassword(false) })
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                } else if (!isSignUp && !viewModel.getState().isLogin) {
                    MyButton(stringResource(R.string.login_title), onClick = { viewModel.action.onLogIn(username, password) })
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                    Spacer(modifier = Modifier.size(36.dp))
                    LoginAnswer(isSignUp = isSignUp, onToggle = { viewModel.action.changeSignScreen() })
                    Spacer(modifier = Modifier.size(8.dp))
                    SendEmail(username, viewModel.action.callResetPasswordEmail)
                } else if (isSignUp && !viewModel.getState().isLogin) {
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = { Text(stringResource(R.string.password_confirm)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading
                    )
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                    Spacer(modifier = Modifier.size(8.dp))
                    MyButton(stringResource(R.string.signup_title), onClick = {
                        viewModel.action.onSignOn(username, password, passwordConfirm)
                    })
                    Spacer(modifier = Modifier.size(36.dp))
                    LoginAnswer(isSignUp = isSignUp, onToggle = { viewModel.action.changeSignScreen() })
                } else {
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                    MyButton("Log Out", onClick = {
                        viewModel.action.onLogOut()
                        username = ""
                        password = ""
                    })
                    Spacer(modifier = Modifier.size(8.dp))
                    MyButton("Cambia Password", onClick = {
                        viewModel.action.toggleUpdatePassword(true)
                        password = ""
                        passwordConfirm = ""
                    })
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

@Composable
fun LoginAnswer(isSignUp: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = if (!isSignUp) stringResource(R.string.login_answer) else stringResource(R.string.signup_answer))
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = if (!isSignUp) stringResource(R.string.signup_title) else stringResource(R.string.login_title),
            color = Color.Green,
            modifier = Modifier.clickable { onToggle() }
        )
        Text(text = ".")
    }
}

@Composable
fun SendEmail(email: String, onToggle: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Vuoi cambiare password? ")
        Text(
            text = "E-mail",
            color = Color.Green,
            modifier = Modifier.clickable { onToggle(email) }
        )
        Text(text = ".")
    }
}

@Composable
fun ErrorText(viewModel: LoginScreenViewModel) {
    viewModel.errorMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SuccessText(viewModel: LoginScreenViewModel) {
    viewModel.successMessage?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
