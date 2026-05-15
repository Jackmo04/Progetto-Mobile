package com.example.cacciaaltesoro.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.minutes
import io.github.jan.supabase.storage.storage
import java.io.File

interface LoginRepository {
    suspend fun setUsername(username: String) : Preferences
    suspend fun setUserId(userId: String) : Preferences
    suspend fun setIsSignUp(isSignUp: Boolean) : Preferences
    suspend fun clearSession():Preferences
    suspend fun onLogIn(username: String, password: String)
    suspend fun logOut()
    suspend fun getLoggedUser() : UserInfo?
    suspend fun getImageFromBucket(uid: String): String?
    suspend fun uploadProfileImage(context: Context, uid: String, imageBytes: ByteArray, fileName: String)
}
class LoginRepositoryImpl (
    private val dataStore: DataStore<Preferences>,
    val supabase: SupabaseClient
): LoginRepository {
    val authStatus = supabase.auth.sessionStatus
    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USERNAME_UUID = stringPreferencesKey("idUser")
        private val PASSWORD = stringPreferencesKey("password")
        private val IS_SIGN_UP = booleanPreferencesKey("isSignUp")
    }

    val username = dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val userId = dataStore.data.map { it[USERNAME_UUID] ?: "" }
    val isSignUp = dataStore.data.map { it[IS_SIGN_UP] ?: false }
    private val _isPasswordUpdateRequested = MutableStateFlow(false)


    override suspend fun setUsername(username: String) = dataStore.edit { it[USERNAME_KEY] = username }
    override suspend fun setUserId(userId: String) = dataStore.edit { it[USERNAME_UUID] = userId }
    override suspend fun setIsSignUp(isSignUp: Boolean) = dataStore.edit { it[IS_SIGN_UP] = isSignUp }

    override suspend fun clearSession() = dataStore.edit {
        it.remove(USERNAME_KEY)
        it.remove(USERNAME_UUID)
        it.remove(PASSWORD)
        it.remove(IS_SIGN_UP)

    }

    override suspend fun onLogIn(username: String, password: String) {
        try {
            supabase.auth.signInWith(Email) {
                this.email = username
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                setUserId(userId)
                setUsername(username)
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
                setIsSignUp(false)
            }
            Log.i("LoginDebug", "Registrazione eseguita con successo")
        } catch (e: Exception) {
            Log.e("LoginDebug", "Errore durante la registrazione!", e)
            throw e
        }
    }

    override suspend fun logOut() {
        try {
            supabase.auth.signOut()
            clearSession()
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

    override suspend fun getLoggedUser(): UserInfo? {
        return supabase.auth.currentSessionOrNull()?.user
    }


    val isPasswordUpdateRequested = _isPasswordUpdateRequested.asStateFlow()

    fun setPasswordUpdateRequested(value: Boolean) {
        _isPasswordUpdateRequested.value = value
    }


    override suspend fun getImageFromBucket(uid: String): String? {
        val bucketName = "Upload"
        return try {
            val userDto = supabase.from(SupabaseTables.USERS.tableName).select {
                filter {
                    UserDTO::uuid eq uid
                }
            }.decodeSingleOrNull<UserDTO>()

            val imgName = userDto?.image
            if (imgName.isNullOrEmpty()) return null

            supabase.storage.from(bucketName)
                .createSignedUrl(path = imgName, expiresIn = 60.minutes)

        } catch (e: Exception) {
            Log.e("Image2", e.toString())
            e.printStackTrace()
            null
        }
    }

    override suspend fun uploadProfileImage(context: Context, uid: String, imageBytes: ByteArray, fileName: String) {
        try {
            val tempFile = File(context.cacheDir, "temp_original.jpg")
            tempFile.writeBytes(imageBytes)

            val compressedFile = Compressor.compress(context, tempFile) {
                resolution(800, 800)
                quality(75)
                format(Bitmap.CompressFormat.JPEG)
            }

            val compressedBytes = compressedFile.readBytes()

            supabase.storage.from("Upload").upload(path = fileName, data = compressedBytes) {
                upsert = true
            }

            supabase.from(SupabaseTables.USERS.tableName).update(
                {
                    UserDTO::image setTo fileName
                }
            ) {
                filter {
                    UserDTO::uuid eq uid
                }
            }

            tempFile.delete()
            compressedFile.delete()

        } catch (e: Exception) {
            Log.e("UploadError", "Errore durante l'upload dell'immagine", e)
            throw e
        }
    }}
