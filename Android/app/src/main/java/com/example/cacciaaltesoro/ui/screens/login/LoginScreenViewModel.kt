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
import androidx.annotation.StringRes
import com.example.cacciaaltesoro.R


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
    val onLogOut: (()-> Unit) -> Unit,
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

    @get:StringRes
    var errorMessage by mutableStateOf<Int?>(null)
        private set

    @get:StringRes
    var successMessage by mutableStateOf<Int?>(null)
        private set



    val action = LoginAction(
        onLogIn = { username, password ->
            if(username.isBlank() || password.isBlank()){
                errorMessage= R.string.enter_email_and_password
                return@LoginAction
            }
            enableLoading()
            viewModelScope.launch {
               enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.onLogIn(username, password)
                    successMessage = R.string.login_done_with_success
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = mapSupabaseError(e)
                } finally {
                    disableLoading()
                }
            }
        },
        onSignOn = { username, password, passwordConfirm ->
            viewModelScope.launch {
                if (password != passwordConfirm) {
                    errorMessage = R.string.passwords_not_equal
                    return@launch
                }
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.onSignOn(username, password)
                    successMessage = R.string.sing_complete
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = mapSupabaseError(e)
                } finally {
                   disableLoading()
                }
            }
        },
        onLogOut = { onComplete ->
            viewModelScope.launch {
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.logOut()
                    _state.update { currentState ->
                        currentState.copy(
                            isLogin = false,
                            imageUri = null,
                        )
                    }
                    successMessage = R.string.logout_done
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = mapSupabaseError(e)
                } finally {
                   disableLoading()
                }
                onComplete()

            }
        },
        changeSignScreen = {
            viewModelScope.launch {
                repository.setIsSignUp(!_state.value.isSignUp)
            }
        },
        callResetPasswordEmail={email->
            viewModelScope.launch {

                if(email.isBlank()){
                    errorMessage= R.string.need_email_for_reset
                    return@launch
                }
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.sendResetPasswordEmail(email)
                    successMessage = R.string.reset_email_sent
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = mapSupabaseError(e)
                } finally {
                    disableLoading()
                }
            }
        },
        changePassword = { password, passwordConfirm ->
            viewModelScope.launch {
                if (password != passwordConfirm) {
                    errorMessage = R.string.passwords_not_equal
                    return@launch
                }
                enableLoading()
                errorMessage = null
                successMessage = null
                try {
                    repository.updatePassword(password)
                    successMessage = R.string.password_update

                    repository.setPasswordUpdateRequested(false)
                    _state.update {
                        it.copy(isUpdatePassword = false)
                    }
                } catch (e: Exception) {
                    Log.e("Login" , e.toString())
                    errorMessage = mapSupabaseError(e)
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
                        successMessage = R.string.profile_image_updated
                    }
                } catch (e: Exception) {
                    Log.e("UploadPhoto" , e.toString())
                    errorMessage = R.string.error_during_image_update
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

    @StringRes
    private fun mapSupabaseError(e: Exception): Int {
        val errorMessage = e.message?.lowercase() ?: ""

        return when {
            errorMessage.contains("invalid login credentials") -> R.string.email_password_not_valid
            errorMessage.contains("email not confirmed") -> R.string.email_not_confirmed

            errorMessage.contains("user already registered") -> R.string.account_exists
            errorMessage.contains("password should be at least") -> R.string.password_too_weak

            errorMessage.contains("unable to resolve host") ||
                    errorMessage.contains("failed to connect") -> R.string.connection_error

            else -> R.string.unexpected_error
        }
    }
}
