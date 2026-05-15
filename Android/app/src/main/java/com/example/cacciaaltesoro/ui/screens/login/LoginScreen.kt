package com.example.cacciaaltesoro.ui.screens.login

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cacciaaltesoro.R
import com.example.cacciaaltesoro.ui.composables.AppBar
import com.example.cacciaaltesoro.utils.rememberCameraLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginScreenViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
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

    fun processAndUploadImage(imageUri: Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    withContext(Dispatchers.Main) {
                        viewModel.action.uploadImage(context, imageUri, bytes)
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageProcessing", "Errore nel caricamento dell'immagine", e)
            }
        }
    }

    val (_, takePicture) = rememberCameraLauncher(
        onPictureTaken = { imageUri -> processAndUploadImage(imageUri) }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { processAndUploadImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePicture()
        } else {
            Toast.makeText(context, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = { AppBar(title, navController) }
    ) { contentPadding ->

        if (state.isInitializing) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {

                if (!isUpdatePassword) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { text -> username = text },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        readOnly = state.isLogin,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = if (state.isLogin) ImeAction.Done else ImeAction.Next
                        )
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
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done
                            )
                        )

                        if (isSignUp) {
                            Spacer(modifier = Modifier.size(8.dp))
                            OutlinedTextField(
                                value = passwordConfirm,
                                onValueChange = { passwordConfirm = it },
                                label = { Text(stringResource(R.string.password_confirm)) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = { passwordConfirm = it },
                        label = { Text(stringResource(R.string.password_confirm)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))
                ErrorText(viewModel)
                SuccessText(viewModel)
                Spacer(modifier = Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .animateContentSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isUpdatePassword) {
                                MyButton(stringResource(R.string.update_password_title), onClick = {
                                    viewModel.action.changePassword(password, passwordConfirm)
                                })
                                Spacer(modifier = Modifier.size(8.dp))
                                MyButton(stringResource(R.string.cancel), onClick = { viewModel.action.toggleUpdatePassword(false) })

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
                                Spacer(Modifier.size(24.dp))
                                Text("Aggiorna la tua foto", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.size(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = { checkAndRequestCameraPermission() },
                                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    ) {
                                        Icon(
                                            Icons.Outlined.PhotoCamera,
                                            contentDescription = "Fotocamera",
                                            modifier = Modifier.size(ButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Fotocamera")
                                    }

                                    Button(
                                        onClick = {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    ) {
                                        Icon(
                                            Icons.Outlined.PhotoLibrary,
                                            contentDescription = "Galleria",
                                            modifier = Modifier.size(ButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Galleria")
                                    }
                                }
                                Spacer(Modifier.size(16.dp))
                                AsyncImage(
                                    model = state.imageUri,
                                    contentDescription = "Foto del profilo",
                                    modifier = Modifier
                                        .size(140.dp)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
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
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onToggle() }.padding(4.dp)
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
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onToggle(email) }.padding(4.dp)
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
            style = MaterialTheme.typography.bodyMedium,
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
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}