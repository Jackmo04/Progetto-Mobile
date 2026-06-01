package com.example.cacciaaltesoro.data.database

enum class SupabaseTables(val tableName : String) {
    USERS("utenti"),
    EVENTS("partite"),
    TAGS("tags"),
    TAG_CACHED("tagraccolti"),
    SUBSCRIPTION("partecipazioni"),
    FOUND_TAGS_VIEW("found_tags_event");

    override fun toString(): String {
        return this.tableName
    }
}