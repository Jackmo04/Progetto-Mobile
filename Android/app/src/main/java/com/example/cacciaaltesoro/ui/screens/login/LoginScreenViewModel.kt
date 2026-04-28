package com.example.cacciaaltesoro.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val username: String,
    val password: String,
    val userId: String,
    val isLogin: Boolean
)

data class LoginAction(
    val onLogIn: (String, String) -> Unit,
    val onSignOn: (String, String) -> Unit,
    val onLogOut: () -> Unit
)

class LoginScreenViewModel(
    private val repository: LoginRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState("","","", false))

    fun getState ():LoginState {return _state.value}

    init {
        viewModelScope.launch {
            _state.update { it.copy(username = repository.username.first(), password = repository.password.first(), userId = repository.userId.first() , isLogin = repository.isLogin.first()) }
        }
    }

    val action = LoginAction(
        onLogIn = { username, password ->
            viewModelScope.launch {
                repository.onLogIn(username, password)
            }
        },
        onSignOn = { username, password ->
            viewModelScope.launch {
                repository.onSignOn(username, password)
            }
        },
        onLogOut = {
            viewModelScope.launch {
                repository.setIsLogin(false)
            }
        }
    )
}
