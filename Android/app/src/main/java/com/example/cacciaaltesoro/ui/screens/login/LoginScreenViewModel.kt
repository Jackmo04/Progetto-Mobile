package com.example.cacciaaltesoro.ui.screens.login

import android.net.Uri
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

data class LoginState(
    val username: String = "",
    val userId: String = "",
    val isLogin: Boolean = false,
    val isSignUp: Boolean = false,
    val isUpdatePassword: Boolean = false,
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true,
    val imageUri: Uri? = null,

    val showLocationDisabledAlert: Boolean = false,
    val showPermissionDeniedAlert: Boolean = false,
    val showPermissionPermanentlyDeniedSnackbar: Boolean = false,
    val showNoConnectivitySnackbar: Boolean = false
)

data class LoginAction(
    val onLogIn: (String, String) -> Unit,
    val onSignOn: (String, String, String) -> Unit,
    val onLogOut: () -> Unit,
    val changeSignScreen: () -> Unit,
    val callResetPasswordEmail:(String) -> Unit,
    val changePassword: (String, String, String) -> Unit,
    val toggleUpdatePassword: (Boolean) -> Unit,
    val setImageUri: (Uri?) -> Unit,
    val getImageFromCloud:() -> Unit,
    val uploadImage: (Uri, ByteArray) -> Unit,

    val setShowLocationDisabledAlert: (Boolean) -> Unit,
    val setShowPermissionDeniedAlert: (Boolean) -> Unit,
    val setShowPermissionPermanentlyDeniedSnackbar: (Boolean) -> Unit,
    val setShowNoConnectivitySnackbar: (Boolean) -> Unit
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
                    errorMessage = "Errore nell'invio dell'email"
                } finally {
                    disableLoading()
                }
            }
        },
        changePassword = { username, password, passwordConfirm ->
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
        setImageUri ={ imageUri ->
            _state.update { it.copy(imageUri = imageUri) } },
        getImageFromCloud = {
            viewModelScope.launch {
                val uid = _state.value.userId
                if (uid.isNotEmpty()) {
                    val url = repository.getImageFromBucket(uid)
                    if (url != null) {
                        // Convertiamo l'URL stringa di Supabase in un Uri leggibile da Jetpack Compose
                        _state.update { it.copy(imageUri = android.net.Uri.parse(url)) }
                    }
                }
            }
        },

        uploadImage = { uri, imageBytes ->
            viewModelScope.launch {
                enableLoading()
                try {
                    val uid = _state.value.userId
                    if (uid.isNotEmpty()) {
                        val fileName = "profile_${uid}.jpg"

                        // Chiama il DB
                        repository.uploadProfileImage(uid, imageBytes, fileName)

                        // Aggiorna la UI
                        _state.update { it.copy(imageUri = uri) }
                        successMessage = "Immagine del profilo aggiornata!"
                    }
                } catch (e: Exception) {
                    errorMessage = "Errore durante il caricamento della foto"
                } finally {
                    disableLoading()
                }
            }
        },

        setShowLocationDisabledAlert ={ show ->
            _state.update { it.copy(showLocationDisabledAlert = show) } },
        setShowPermissionDeniedAlert ={ show ->
            _state.update { it.copy(showPermissionDeniedAlert = show) } },
        setShowPermissionPermanentlyDeniedSnackbar={ show ->
            _state.update { it.copy(showPermissionPermanentlyDeniedSnackbar = show) } },
        setShowNoConnectivitySnackbar={ show ->
            _state.update { it.copy(showNoConnectivitySnackbar = show) } },
    )

    init {
        viewModelScope.launch {
            combine(
                repository.isLogin,
                repository.username,
                repository.userId,
                repository.isSignUp,
                repository.isPasswordUpdateRequested
            ) { isLogin, username, userId, isSignUp, isRequested ->
                _state.value.copy(
                    isLogin = isLogin,
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
