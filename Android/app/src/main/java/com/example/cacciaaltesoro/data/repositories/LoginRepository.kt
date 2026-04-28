package com.example.cacciaaltesoro.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class LoginRepository(
    private val dataStore: DataStore<Preferences>,
    private val supabase: SupabaseClient
) {
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USERNAME_UUID = stringPreferencesKey("idUser")

        private val IS_LOGIN = booleanPreferencesKey("isLogin")
    }

    val username = dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val userId = dataStore.data.map { it[USERNAME_UUID] ?: "" }
    val isLogin = dataStore.data.map { it[IS_LOGIN]?: false }

    suspend fun setUsername(username: String) = dataStore.edit { it[USERNAME_KEY] = username }

    suspend fun setUserId(userId: String) = dataStore.edit { it[USERNAME_UUID] = userId }

    suspend fun setIsLogin(isLogin: Boolean) = dataStore.edit { it[IS_LOGIN] = isLogin }

    suspend fun clearSession() = dataStore.edit {
        it.remove(USERNAME_KEY)
        it.remove(USERNAME_UUID)
        it.remove(IS_LOGIN)
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

                }
    setIsLogin(true)
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante il login!", e)
            }

    }

    suspend fun onSignUp(username: String, password: String) {
            try {

                supabase.auth.signUpWith(Email) {
                    this.email = username
                    this.password = password
                }
               setIsLogin(true)
                Log.i("LoginDebug", "Registrazione eseguita con successo")
            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore durante la registrazione!", e)
            }
        }

    suspend fun logOut(){
            try {
                supabase.auth.signOut()
                clearSession()
                setIsLogin(false)

            } catch (e: Exception) {
                Log.e("LoginDebug", "Errore nel Log Out", e)
            }
    }
}

