package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.api.Notification
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class NotificationDAO(private val supabase: SupabaseClient) {

    suspend fun getAllNotify(): List<Notification>?{
        return try {
            supabase.from(TableName.NOTIFICHE.tableName).select(columns = Columns.raw("*, utenti!notifiche_not_utente_fkey(*)")).decodeList<Notification>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}