package com.example.cacciaaltesoro.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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


    val state by viewModel.state.collectAsState()
    val isUpdatePassword = state.isUpdatePassword
    val isSignUp = state.isSignUp

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.username) {
        if (state.username.isNotEmpty() && username.isEmpty()) {
            username = state.username
        }
    }

    val title = if (state.isLogin) {
        if (isUpdatePassword) stringResource(R.string.update_password_title) else stringResource(R.string.profile_title)
    } else if (isUpdatePassword) {
        stringResource(R.string.update_password_title)
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
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    readOnly = state.isLogin
                )

                if (!state.isLogin) {
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                    )


                    if (isSignUp) {
                        Spacer(modifier = Modifier.size(8.dp))
                        OutlinedTextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            label = { Text(stringResource(R.string.password_confirm)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.new_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text(stringResource(R.string.password_confirm)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.size(16.dp))
            ErrorText(viewModel)
            SuccessText(viewModel)
            Spacer(modifier = Modifier.size(8.dp))


            Box(
                modifier = Modifier.fillMaxWidth().requiredSize(200.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (state.isLoading) { // <-- CORRETTO DA isLogin A isLoading!
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isUpdatePassword) {
                            MyButton("Aggiorna Password", onClick = {
                                viewModel.action.changePassword(username, password, passwordConfirm)
                            })
                            Spacer(modifier = Modifier.size(8.dp))
                            MyButton("Annulla", onClick = { viewModel.action.toggleUpdatePassword(false) })

                        } else if (!isSignUp && !state.isLogin) {
                            MyButton(stringResource(R.string.login_title), onClick = { viewModel.action.onLogIn(username, password) })
                            Spacer(modifier = Modifier.size(36.dp))
                            LoginAnswer(isSignUp = false, onToggle = { viewModel.action.changeSignScreen() })
                            Spacer(modifier = Modifier.size(8.dp))
                            SendEmail(username, viewModel.action.callResetPasswordEmail)

                        } else if (isSignUp && !state.isLogin) {
                            MyButton(stringResource(R.string.signup_title), onClick = {
                                viewModel.action.onSignOn(username, password, passwordConfirm)
                            })
                            Spacer(modifier = Modifier.size(36.dp))
                            LoginAnswer(isSignUp = true, onToggle = { viewModel.action.changeSignScreen() })

                        } else {
                            MyButton("Log Out", onClick = {
                                viewModel.action.onLogOut()
                                username = ""
                                password = ""
                            })
                            Spacer(modifier = Modifier.size(8.dp))
                            MyButton(stringResource(R.string.change_password), onClick = {
                                viewModel.action.toggleUpdatePassword(true)
                                password = ""
                                passwordConfirm = ""
                            })
                        }
                    }
                }
            }
        }}}

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
