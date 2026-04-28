package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.UserDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
class UserDAO(private val supabase: SupabaseClient) {


    suspend fun getUserByUsername(username: String): UserDTO?{
        return try {
            supabase.from(SupabaseTables.USERS.tableName).select {
                filter {
                    UserDTO::username eq username
                }
            }.decodeSingleOrNull<UserDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUsers(): List<UserDTO>?{
        return try {
            supabase.from(SupabaseTables.USERS.tableName).select().decodeList<UserDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUserSMatch(username: String): UserDTO? {
        return try {
            // Using the relationship for organized matches
            supabase.from(SupabaseTables.USERS.tableName).select(
                columns = Columns.raw("*, partite!partite_par_organizzatore_fkey(*)")
            ) {
                filter { UserDTO::username eq username }
            }.decodeSingleOrNull<UserDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllUserSCatchesTag(username: String, partita: Int): UserDTO? {
        return try {
            // We fetch the user and filter the nested tags by the partita ID
            supabase.from(SupabaseTables.USERS.tableName).select(
                columns = Columns.raw("*, tags!tagraccolti(*)")
            ) {
                filter { 
                    UserDTO::username eq username
                }
            }.decodeSingleOrNull<UserDTO>()?.let { user ->
                user.copy(tagDTOS = user.tagDTOS.filter { it.eventId == partita })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

