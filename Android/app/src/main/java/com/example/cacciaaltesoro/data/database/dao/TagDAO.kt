package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.SupabaseTables
import com.example.cacciaaltesoro.data.database.dto.TagDTO
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class TagDAO(private val supabase: SupabaseClient) {

    suspend fun getTagByPosMatch( pos: Int , partita: Int): TagDTO?{
        return try {
            supabase.from(SupabaseTables.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
                filter {
                    TagDTO::number eq pos
                    TagDTO::eventId eq partita
                }
            }.decodeSingleOrNull<TagDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun getTagByUUID( id: String): TagDTO?{
        return try {
            supabase.from(SupabaseTables.TAGS.tableName).select(columns = Columns.raw("*, partite(*)")) {
                filter {
                    TagDTO::id eq id
                }
            }.decodeSingleOrNull<TagDTO>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

}