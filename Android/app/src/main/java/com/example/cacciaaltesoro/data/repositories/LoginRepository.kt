package com.example.cacciaaltesoro.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class LoginRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USERNAME_UUID = stringPreferencesKey("idUser")
    }

    val username = dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val userId = dataStore.data.map { it[USERNAME_UUID] ?: "" }

    suspend fun setUsername(username: String) = dataStore.edit { it[USERNAME_KEY] = username }

    suspend fun setUserId(userId: String) = dataStore.edit { it[USERNAME_UUID] = userId }

    suspend fun clearSession() = dataStore.edit {
        it.remove(USERNAME_KEY)
        it.remove(USERNAME_UUID)
    }

}
