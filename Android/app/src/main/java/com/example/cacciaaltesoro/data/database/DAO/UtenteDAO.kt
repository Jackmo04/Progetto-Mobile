package com.example.cacciaaltesoro.data.database.DAO

import android.R
import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.database.api.Utente
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder
import io.github.jan.supabase.postgrest.query.Columns
class UtenteDAO() {


    suspend public fun getUserByUsername( username: String): Utente?{
        return try {
            Supabase.supabase.from(TableName.UTENTI.tableName).select {
                filter {
                    Utente::ute_username eq username
                }
            }.decodeSingleOrNull<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend public fun getAllUser(): List<Utente>?{
        return try {
            Supabase.supabase.from(TableName.UTENTI.tableName).select().decodeList<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getAllUserSMatch(username: String): Utente? {
        return try {
            // Using the relationship for organized matches
            Supabase.supabase.from(TableName.UTENTI.tableName).select(
                columns = Columns.raw("*, partite!partite_par_organizzatore_fkey(*)")
            ) {
                filter { Utente::ute_username eq username }
            }.decodeSingleOrNull<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllUserSCatchesTag(username: String, partita: Int): Utente? {
        return try {
            // We fetch the user and filter the nested tags by the partita ID
            Supabase.supabase.from(TableName.UTENTI.tableName).select(
                columns = Columns.raw("*, tags!tagraccolti(*)")
            ) {
                filter { 
                    Utente::ute_username eq username
                }
            }.decodeSingleOrNull<Utente>()?.let { user ->
                user.copy(tags = user.tags.filter { it.tag_partita == partita })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

