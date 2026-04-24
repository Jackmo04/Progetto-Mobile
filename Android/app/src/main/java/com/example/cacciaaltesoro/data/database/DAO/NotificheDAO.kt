package com.example.cacciaaltesoro.data.database.DAO

import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.database.api.Notifiche
import com.example.cacciaaltesoro.data.database.api.Utente
import io.github.jan.supabase.postgrest.from

class NotificheDAO() {

    suspend public fun getAllNotify(): List<Notifiche>?{
        return try {
            Supabase.supabase.from(TableName.NOTIFICHE.tableName).select().decodeList<Notifiche>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}