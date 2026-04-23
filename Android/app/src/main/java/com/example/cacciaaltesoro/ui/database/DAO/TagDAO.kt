package com.example.cacciaaltesoro.ui.database.DAO

import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Partita
import com.example.cacciaaltesoro.ui.database.api.Tag
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.postgrest.from

class TagDAO() {


    suspend public fun getTagByID( id: Int): Tag?{
        return try {
            Supabase.supabase.from(TableName.TAGS.tableName).select {
                filter {
                    Tag::tag_id eq id
                }
            }.decodeSingleOrNull<Tag>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend public fun getAllUsere(): List<Utente>?{
        return try {
            Supabase.supabase.from(TableName.UTENTI.tableName).select().decodeList<Utente>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }
}