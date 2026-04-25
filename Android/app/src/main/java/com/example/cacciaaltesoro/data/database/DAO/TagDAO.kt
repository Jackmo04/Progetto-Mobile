package com.example.cacciaaltesoro.data.database.DAO

import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.database.api.Tag
import com.example.cacciaaltesoro.data.database.api.Utente
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class TagDAO() {


    suspend public fun getTagByID( id: Int): Tag?{
        return try {
            Supabase.supabase.from(TableName.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
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