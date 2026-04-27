package com.example.cacciaaltesoro.data.database.DAO

import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.database.api.Tag
import com.example.cacciaaltesoro.data.database.api.Utente
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class TagDAO(private val supabase: SupabaseClient) {


    suspend public fun getTagByID( id: Int): Tag?{
        return try {
            supabase.from(TableName.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
                filter {
                    Tag::tag_id eq id
                }
            }.decodeSingleOrNull<Tag>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

}