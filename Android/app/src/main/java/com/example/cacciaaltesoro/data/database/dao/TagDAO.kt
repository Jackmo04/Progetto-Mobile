package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.api.Tag
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class TagDAO(private val supabase: SupabaseClient) {

    suspend fun getTagByPosMatch( pos: Int , partita: Int): Tag?{
        return try {
            supabase.from(TableName.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
                filter {
                    Tag::number eq pos
                    Tag::eventId eq partita
                }
            }.decodeSingleOrNull<Tag>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getTagByUUID( id: String): Tag?{
        return try {
            supabase.from(TableName.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
                filter {
                    Tag::id eq id
                }
            }.decodeSingleOrNull<Tag>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

}