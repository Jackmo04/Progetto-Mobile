package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.ui.database.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import junit.framework.AssertionFailedError
import kotlinx.coroutines.test.runTest
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

    @Test
    fun `test login` () = runTest{
        val conn = Supabase.supabase;
        val email = "mattia.cavina2@studio.unibo.it"
        val username = email
        val password = "pwd123"

        try {
            Supabase.login(email,password)
        }
        catch ( e: Exception){
            assertEquals(1,2)
        }


    }
}