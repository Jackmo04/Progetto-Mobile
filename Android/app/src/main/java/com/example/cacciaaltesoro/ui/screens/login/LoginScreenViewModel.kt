package com.example.cacciaaltesoro.ui.screens.login

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import io.github.jan.supabase.auth.status.SessionStatus

data class LoginState(
    val username: String = "",
    val userId: String = "",
    val isLogin: Boolean = false,
    val isSignUp: Boolean = false,
    val isUpdatePassword: Boolean = false,
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true,
    val imageUri: Uri? = null
)

data class LoginAction(
    val onLogIn: (String, String) -> Unit,
    val onSignOn: (String, String, String) -> Unit,
    val onLogOut: () -> Unit,
    val changeSignScreen: () -> Unit,
    val callResetPasswordEmail:(String) -> Unit,
    val changePassword: (String, String) -> Unit,
    val toggleUpdatePassword: (Boolean) -> Unit,
    val getImageFromCloud:() -> Unit,
    val uploadImage: (Context ,Uri, ByteArray) -> Unit
)

class LoginScreenViewModel(
    private val repository: LoginRepositoryImpl
) : ViewModel() {

    private var _state = MutableStateFlow(LoginState())

    val  state =_state.asStateFlow()

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set



    val action = LoginAction(
        onLogIn = { username, password ->
            enableLoading()
            viewModelScope.launch {
               enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.onLogIn(username, password)
                    successMessage = "Login eseguito con successo"
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = "Errore durante il login: Email o Password non corrette"
                } finally {
                    disableLoading()
                }
            }
        },
        onSignOn = { username, password, passwordConfirm ->
            viewModelScope.launch {
                if (password != passwordConfirm) {
                    errorMessage = "Le password non coincidono"
                    return@launch
                }
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.onSignOn(username, password)
                    successMessage = "Registrazione completata"
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = "Errore durante la registrazione"
                } finally {
                   disableLoading()
                }
            }
        },
        onLogOut = {
            viewModelScope.launch {
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.logOut()
                    successMessage = "Logout effettuato"
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = "Errore durante il logout"
                } finally {
                   disableLoading()
                }
            }
        },
        changeSignScreen = {
            viewModelScope.launch {
                repository.setIsSignUp(!_state.value.isSignUp)
            }
        },
        callResetPasswordEmail={email->
            viewModelScope.launch {
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.sendResetPasswordEmail(email)
                    successMessage = "Email di reset inviata"
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = "Errore nell'invio dell'email"
                } finally {
                    disableLoading()
                }
            }
        },
        changePassword = { password, passwordConfirm ->
            viewModelScope.launch {
                if (password != passwordConfirm) {
                    errorMessage = "Le password non coincidono"
                    return@launch
                }
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.updatePassword(password)
                    successMessage = "Password aggiornata"

                    repository.setPasswordUpdateRequested(false)
                    _state.update {
                        it.copy(isUpdatePassword = false)
                    }
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = "Errore durante il cambio password"
                } finally {
                    disableLoading()
                }
            }
        },

        toggleUpdatePassword = { isVisible ->
            repository.setPasswordUpdateRequested(isVisible)
            _state.update {
                it.copy(isUpdatePassword = isVisible)
            }
        },
        getImageFromCloud = {
            viewModelScope.launch {
                val uid = _state.value.userId
                if (uid.isNotEmpty()) {
                    val url = repository.getImageFromBucket(uid)
                    if (url != null) {
                        _state.update { it.copy(imageUri = url.toUri()) }
                    }else{
                        _state.update { it.copy(imageUri = null) }
                    }
                }
            }
        },

        uploadImage = { ctx,uri, imageBytes ->
            viewModelScope.launch {
                enableLoading()
                try {
                    val uid = _state.value.userId
                    if (uid.isNotEmpty()) {
                        val fileName = "profile_${uid}.jpg"

                        repository.uploadProfileImage(ctx,uid, imageBytes, fileName)

                        _state.update { it.copy(imageUri = uri) }
                        successMessage = "Immagine del profilo aggiornata!"
                    }
                } catch (e: Exception) {
                    Log.e("UploadPhoto" , e.toString())
                    errorMessage = "Errore durante il caricamento della foto"
                } finally {
                    disableLoading()
                }
            }
        }
    )

    init {
        viewModelScope.launch {
            combine(
                repository.authStatus,
                repository.username,
                repository.isSignUp,
                repository.isPasswordUpdateRequested
            ) { authStatus, username, isSignUp, isRequested ->
                val isUserActuallyLoggedIn = authStatus is SessionStatus.Authenticated
                val userId = repository.getLoggedUser()?.id ?: ""
                _state.value.copy(
                    isLogin = isUserActuallyLoggedIn,
                    username = username,
                    userId = userId,
                    isSignUp = isSignUp,
                    isUpdatePassword = isRequested,
                    isInitializing = false
                )
            }.collect { newState ->
                _state.value = newState

                if (newState.isLogin && newState.userId.isNotEmpty() && newState.imageUri == null) {
                    action.getImageFromCloud()
                }
            }
        }
    }

    fun disableLoading(){
            _state.update { it.copy(isLoading = false) }

    }
    fun enableLoading(){
            _state.update { it.copy(isLoading = true) }

    }
}
