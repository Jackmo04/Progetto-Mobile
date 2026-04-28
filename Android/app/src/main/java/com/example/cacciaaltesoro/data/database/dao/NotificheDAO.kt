package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.api.Notifiche
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class NotificheDAO(private val supabase: SupabaseClient) {

    suspend fun getAllNotify(): List<Notifiche>?{
        return try {
            supabase.from(TableName.NOTIFICHE.tableName).select(columns = Columns.raw("*, utenti!notifiche_not_utente_fkey(*)")).decodeList<Notifiche>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}