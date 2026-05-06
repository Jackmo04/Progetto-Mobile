package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class LoginRepository(
    private val dataStore: DataStore<Preferences>,
    private val supabase: SupabaseClient
) {
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USERNAME_UUID = stringPreferencesKey("idUser")
        private val PASSWORD = stringPreferencesKey("password")
        private val IS_LOGIN = booleanPreferencesKey("isLogin")

        private val IS_SIGN_UP = booleanPreferencesKey("isSignUp")
    }

    val username = dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val password = dataStore.data.map { it[PASSWORD] ?: "" }
    val userId = dataStore.data.map { it[USERNAME_UUID] ?: "" }
    val isLogin = dataStore.data.map { it[IS_LOGIN] ?: false }
    val isSignUp = dataStore.data.map { it[IS_SIGN_UP] ?: false }
    private val _isPasswordUpdateRequested = MutableStateFlow(false)


    suspend fun setUsername(username: String) = dataStore.edit { it[USERNAME_KEY] = username }
    suspend fun setUserId(userId: String) = dataStore.edit { it[USERNAME_UUID] = userId }
    suspend fun setPassword(password: String) = dataStore.edit { it[PASSWORD] = password }
    suspend fun setIsLogin(isLogin: Boolean) = dataStore.edit { it[IS_LOGIN] = isLogin }
    suspend fun setIsSignUp(isSignUp: Boolean) = dataStore.edit { it[IS_SIGN_UP] = isSignUp }

    suspend fun clearSession() = dataStore.edit {
        it.remove(USERNAME_KEY)
        it.remove(USERNAME_UUID)
        it.remove(PASSWORD)
        it.remove(IS_LOGIN)
        it.remove(IS_SIGN_UP)

    }

    suspend fun onLogIn(username: String, password: String) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = username
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                setUserId(userId)
                setUsername(username)
                setIsLogin(true)
                setIsSignUp(false)
            }
        } catch (e: Exception) {
            Log.e("LoginDebug", "Errore durante il login!", e)
            throw e
        }
    }

    suspend fun onSignOn(username: String, password: String) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = username
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                setUserId(userId)
                setUsername(username)
                setIsLogin(true)
                setIsSignUp(false)
            }
            Log.i("LoginDebug", "Registrazione eseguita con successo")
        } catch (e: Exception) {
            Log.e("LoginDebug", "Errore durante la registrazione!", e)
            throw e
        }
    }

    suspend fun logOut() {
        try {
            supabase.auth.signOut()
            clearSession()
            setIsLogin(false)
            setIsSignUp(false)
        } catch (e: Exception) {
            Log.e("LoginDebug", "Errore nel Log Out", e)
            throw e
        }
    }

    suspend fun sendResetPasswordEmail(email: String) {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "caccia-al-tesoro://reset-password"
        )
    }

    suspend fun updatePassword(newPassword: String){
        try {
            supabase.auth.updateUser {
                password = newPassword
            }
        } catch (e: Exception) {
            Log.e("ChangePassword", "Errore aggiornamento password", e)
            throw e
        }
    }


    val isPasswordUpdateRequested = _isPasswordUpdateRequested.asStateFlow()

    fun setPasswordUpdateRequested(value: Boolean) {
        _isPasswordUpdateRequested.value = value
    }
}
