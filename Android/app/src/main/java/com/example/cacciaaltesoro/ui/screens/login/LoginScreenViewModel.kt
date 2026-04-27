package com.example.cacciaaltesoro.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val repository: LoginRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onLogIn(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Username e password non possono essere vuoti"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                this@LoginScreenViewModel.username = username
                
                supabase.auth.signInWith(Email) {
                    this.email = username
                    this.password = password
                }

                repository.setUsername(username)
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    repository.setUserId(userId)
                }

                Log.i("LoginDebug", "Login eseguito con successo")
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante il login!", e)
                errorMessage = "Errore durante il login: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onSignUp(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Username e password non possono essere vuoti"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                this@LoginScreenViewModel.username = username
                
                supabase.auth.signUpWith(Email) {
                    this.email = username
                    this.password = password
                }

                Log.i("LoginDebug", "Registrazione eseguita con successo")
                errorMessage = "Controlla la tua email per confermare l'account"
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante la registrazione!", e)
                errorMessage = "Errore durante la registrazione: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    init {
        viewModelScope.launch {
            username = repository.username.first()
        }
    }
}
