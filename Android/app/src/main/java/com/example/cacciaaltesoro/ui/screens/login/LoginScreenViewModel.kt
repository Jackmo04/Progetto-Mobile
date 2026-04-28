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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName

data class LoginState(
    val username: String,
    val userId: String,
    val isLogin: Boolean
)

data class LoginAction(
    val onLogin: (String, String) -> Unit,
    val onSignOn: (String, String) -> Unit,
    val onLogOut: () -> Unit
)

class LoginScreenViewModel(
    private val repository: LoginRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState("","", false))

    init {
        viewModelScope.launch {
            _state.update { it.copy(username = repository.username.first(), userId = repository.userId.first() , isLogin = repository.isLogin.first()) }
        }
    }

    val action= LoginAction(
     onLogIn = {(username: String, password: String) {
        isLogin = repository.isLogin.equals("true")
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

                isLogin = true
                successMessage = "Login eseguito correttamente"
                Log.i("LoginDebug", "Login eseguito con successo")

            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante il login!", e)
                errorMessage = "Errore durante il login: Username o Password non corretti"
            } finally {
                isLoading = false
            }
        }
    },

   onSignUp = {(username: String, password: String , passwordConfirm: String) {
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

                isLogin = true
                repository.setIsLogin(true)
                Log.i("LoginDebug", "Registrazione eseguita con successo")
                successMessage = "Registrazione avvenuta con successo!"
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante la registrazione!", e)
                errorMessage = "Errore durante la registrazione: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }},
    onLogOut = (){
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                repository.clearSession()
                isLogin = false
                successMessage = "Logout avvenuto con successo"

            } catch (e: Exception) {
                errorMessage = "Errore nel Log Out"
            }
        }

    }
}
