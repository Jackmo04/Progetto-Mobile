package com.example.cacciaaltesoro.data.repositories

import android.content.Context
import android.graphics.Bitmap
import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.minutes
import io.github.jan.supabase.storage.storage
import java.io.File

interface LoginRepository {
    suspend fun onLogIn(username: String, password: String)
    suspend fun logOut()
    suspend fun getLoggedUser() : UserInfo?
    suspend fun getImageFromBucket(uid: String): String?
    suspend fun uploadProfileImage(context: Context, uid: String, imageBytes: ByteArray, fileName: String)
}
class LoginRepositoryImpl (
    val supabase: SupabaseClient
): LoginRepository {
    val authStatus = supabase.auth.sessionStatus

    private val _isPasswordUpdateRequested = MutableStateFlow(false)
    private val _isFromDeepLink = MutableStateFlow(false)


    override suspend fun onLogIn(username: String, password: String) {
            supabase.auth.signInWith(Email) {
                this.email = username
                this.password = password
            }
    }

    suspend fun onSignOn(username: String, password: String) {
            supabase.auth.signUpWith(Email) {
                this.email = username
                this.password = password
            }
    }

    suspend fun signInWithGoogle(idToken: String) {
        supabase.auth.signInWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
    }

    override suspend fun logOut() {
            supabase.auth.signOut()
    }

    suspend fun sendResetPasswordEmail(email: String) {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "caccia-al-tesoro://reset-password"
        )
    }

    suspend fun updatePassword(newPassword: String){
            supabase.auth.updateUser {
                password = newPassword
            }

    }

    override suspend fun getLoggedUser(): UserInfo? {
        return supabase.auth.currentSessionOrNull()?.user
    }


    val isPasswordUpdateRequested = _isPasswordUpdateRequested.asStateFlow()
    val isFromDeepLink = _isFromDeepLink.asStateFlow()


    fun setPasswordUpdateRequested(value: Boolean, fromDeepLink: Boolean = false) {
        _isPasswordUpdateRequested.value = value
        _isFromDeepLink.value = fromDeepLink
    }


    override suspend fun getImageFromBucket(uid: String): String? {
        val bucketName = "Upload"
            val userDto = supabase.from(SupabaseTables.USERS.tableName).select {
                filter {
                    UserDTO::uuid eq uid
                }
            }.decodeSingleOrNull<UserDTO>()

            val imgName = userDto?.image
            if (imgName.isNullOrEmpty()) return null

            return  supabase.storage.from(bucketName)
                .createSignedUrl(path = imgName, expiresIn = 60.minutes)
    }

    override suspend fun uploadProfileImage(context: Context, uid: String, imageBytes: ByteArray, fileName: String) {
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

    }}
