package com.example.cacciaaltesoro.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val repository: LoginRepository
) : ViewModel() {
    // We are using Compose state directly inside the ViewModel
    // Pro: no need to use .collectAsStateWithLifecycle() in the UI
    // Cons: not thread-safe, ties the ViewModel to Jetpack Compose
    var username by mutableStateOf((""))
        private set

    var password by mutableStateOf((""))
        private set

    fun onLogIn(username: String , password: String ) {
        this.username = username
        viewModelScope.launch {
            repository.setUsername(username)
        }
    }

    fun onSignUp(username: String , password: String  ) {
        //TO DO
    }

    init {
        viewModelScope.launch {
            username = repository.username.first()
        }
    }
}
