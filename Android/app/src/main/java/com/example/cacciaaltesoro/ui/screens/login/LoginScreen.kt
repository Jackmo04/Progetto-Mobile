package com.example.cacciaaltesoro.ui.screens.login

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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withLink
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
    var isSignUp by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.getState().username) {
        if (username.isEmpty()) {
            username = viewModel.getState().username
        }
    }

    val title = if(viewModel.getState().isLogin) {
        stringResource(R.string.profile_title)
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

            OutlinedTextField(
                value = username,
                onValueChange = { text -> username = text },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                readOnly = viewModel.getState().isLogin
            )

            if(!viewModel.getState().isLogin){
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

            Spacer(modifier = Modifier.size(8.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                if (!isSignUp && !viewModel.getState().isLogin) {
                    MyButton(stringResource(R.string.login_title), onClick = { viewModel.action.onLogIn(username, password) })
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                    Spacer(modifier = Modifier.size(36.dp))
                    LoginAnswer(isSignUp = isSignUp, onToggle = { isSignUp = true })
                    SendEmail(username, viewModel.action.callResetPasswordEmail)
                } else if (isSignUp && !viewModel.getState().isLogin ) {
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
                    LoginAnswer(isSignUp = isSignUp, onToggle = { isSignUp = false })
                } else {
                    ErrorText(viewModel)
                    SuccessText(viewModel)
                    MyButton("Log Out", onClick = { viewModel.action.onLogOut()
                        username=""
                        password=""})
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
    val annotatedText = buildAnnotatedString {
        if (!isSignUp) {
            append(stringResource(R.string.login_answer))
            val clickableLink = LinkAnnotation.Clickable(
                tag = "go_to_signup",
                styles = TextLinkStyles(style = SpanStyle(color = Color.Green))
            ) {
                onToggle()
            }
            withLink(clickableLink) {
                append(stringResource(R.string.signup_title))
            }
        } else {
            append(stringResource(R.string.signup_answer))
            val clickableLink = LinkAnnotation.Clickable(
                tag = "go_to_login",
                styles = TextLinkStyles(style = SpanStyle(color = Color.Green))
            ) {
                onToggle()
            }
            withLink(clickableLink) {
                append(stringResource(R.string.login_title))
            }
        }
        append(".")
    }
    Text(text = annotatedText)
}

@Composable
fun SendEmail(email: String, onToggle: (String) -> Unit ) {
    val annotatedText = buildAnnotatedString {
                    append("Vuoi cambiare password?")
                    val clickableLink = LinkAnnotation.Clickable(
                        tag = "go_to_update",
                        styles = TextLinkStyles(style = SpanStyle(color = Color.Green))
                    ) {
                       onToggle(email)
                    }
                    withLink(clickableLink) {
                        append("E-mail")
                    }
        append(".")
    }
    Text(text = annotatedText)
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
