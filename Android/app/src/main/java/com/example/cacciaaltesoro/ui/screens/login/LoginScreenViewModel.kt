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
    var successMessage by mutableStateOf<String?>(null)
        private set


    fun onLogIn(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Username e password non possono essere vuoti"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage= null
            try {
                this@LoginScreenViewModel.username = username
                
                supabase.auth.signInWith(Email) {
                    this.email = username
                    this.password = password
                }


                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    repository.setUserId(userId)
                    repository.setUsername(username)
                    repository.setIsLogin(true)
                }

                successMessage = "Login eseguito correttamente"
                Log.i("LoginDebug", "Login eseguito con successo")

            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante il login!", e)
                errorMessage = "Errore durante il login: Username o Password non corretti"
            } finally {
                isLoading = false
            }
        }
    }

    fun onSignUp(username: String, password: String , passwordConfirm: String) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Username e password non possono essere vuoti"
            return
        }

        if(password!= passwordConfirm){
            errorMessage = "Password e Conferma Password devono essere uguali"
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
                successMessage = "Registrazione avvenuta con successo!"
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante la registrazione!", e)
                errorMessage = "Errore durante la registrazione: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
    fun logOut(){
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                successMessage = "Logout avvenuto con successo"

            } catch (e: Exception) {
                errorMessage = "Errore nel Log Out"

            }
        }

    }

    init {
        viewModelScope.launch {
            username = repository.username.first()
        }
    }
}
