package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.api.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
class UserDAO(private val supabase: SupabaseClient) {


    suspend fun getUserByUsername(username: String): User?{
        return try {
            supabase.from(TableName.UTENTI.tableName).select {
                filter {
                    User::username eq username
                }
            }.decodeSingleOrNull<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUsers(): List<User>?{
        return try {
            supabase.from(TableName.UTENTI.tableName).select().decodeList<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUserSMatch(username: String): User? {
        return try {
            // Using the relationship for organized matches
            supabase.from(TableName.UTENTI.tableName).select(
                columns = Columns.raw("*, partite!partite_par_organizzatore_fkey(*)")
            ) {
                filter { User::username eq username }
            }.decodeSingleOrNull<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllUserSCatchesTag(username: String, partita: Int): User? {
        return try {
            // We fetch the user and filter the nested tags by the partita ID
            supabase.from(TableName.UTENTI.tableName).select(
                columns = Columns.raw("*, tags!tagraccolti(*)")
            ) {
                filter { 
                    User::username eq username
                }
            }.decodeSingleOrNull<User>()?.let { user ->
                user.copy(tags = user.tags.filter { it.eventId == partita })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

