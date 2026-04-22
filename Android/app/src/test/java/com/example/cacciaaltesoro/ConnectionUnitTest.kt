package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.ui.database.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import org.junit.Test

import org.junit.Assert.*

/**
 * Test to connect supabase with application
 */
class ConnectionUnitTest {

    @Test
    fun testConnection() {
        val conn = Supabase.supabase;

        assertEquals("sb_publishable_G8rS-8Q792RWbbTQB3cMUQ_D7lUQHcY",conn.supabaseKey)
    }
}