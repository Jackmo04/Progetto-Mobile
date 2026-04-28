package com.example.cacciaaltesoro.data.database

enum class SupabaseTables(val tableName : String) {
    USERS("utenti"),
    EVENTS("partite"),
    TAGS("tags"),
    NOTIFICATIONS("notifiche");

    override fun toString(): String {
        return this.tableName
    }
}