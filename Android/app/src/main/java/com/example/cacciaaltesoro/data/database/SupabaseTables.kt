package com.example.cacciaaltesoro.data.database

enum class SupabaseTables(val tableName : String) {
    USERS("utenti"),
    EVENTS("partite"),
    TAGS("tags"),
    NOTIFICATIONS("notifiche"),
    TAG_CACHED("tagraccolti"),
    SUBSCRIPTION("partecipazioni");

    override fun toString(): String {
        return this.tableName
    }
}