package com.example.cacciaaltesoro.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class LoginState(
    val username: String = "",
    val userId: String = "",
    val isLogin: Boolean = false
)

data class LoginAction(
    val onLogIn: (String, String) -> Unit,
    val onSignOn: (String, String) -> Unit,
    val onLogOut: () -> Unit
)

class LoginScreenViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private var _state by mutableStateOf(LoginState())
    fun getState() = _state

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.isLogin.collect { isLogin ->
                _state = _state.copy(isLogin = isLogin)
            }
        }
        viewModelScope.launch {
            repository.username.collect { username ->
                _state = _state.copy(username = username)
            }
        }
        viewModelScope.launch {
            repository.userId.collect { userId ->
                _state = _state.copy(userId = userId)
            }
        }
    }

    val action = LoginAction(
        onLogIn = { username, password ->
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                successMessage = null
                try {
                    repository.onLogIn(username, password)
                    successMessage = "Login eseguito con successo"
                } catch (e: Exception) {
                    errorMessage = "Errore durante il login: Email o PAssword non corrette"
                } finally {
                    isLoading = false
                }
            }
        },
        onSignOn = { username, password ->
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                successMessage = null
                try {
                    repository.onSignOn(username, password)
                    successMessage = "Registrazione completata"
                } catch (e: Exception) {
                    errorMessage = "Errore durante la registrazione"
                } finally {
                    isLoading = false
                }
            }
        },
        onLogOut = {
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                successMessage = null
                try {
                    repository.logOut()
                    successMessage = "Logout effettuato"
                } catch (e: Exception) {
                    errorMessage = "Errore durante il logout"
                } finally {
                    isLoading = false
                }
            }
        }
    )
}
