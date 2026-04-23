package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.Supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class ConnectionUnitTest {

    @Test
    fun testConnection() {
        val conn = Supabase.supabase;

        Assert.assertEquals("sb_publishable_G8rS-8Q792RWbbTQB3cMUQ_D7lUQHcY", conn.supabaseKey)
    }

    @Test
    fun `test signup flow`() = runTest {
        val testEmail = "test_utente_${System.currentTimeMillis()}@gmail.com"
        val testPass = "psw123!"
        val testUsername = "tester"

        try {
            Supabase.signUp(email = testEmail, pass = testPass, username = testUsername)
            println("Registrazione riuscita per: $testEmail")
        } catch (e: Exception) {
            Assert.fail("La registrazione è fallita: ${e.message}")
        }
    }

        @Test
        fun `test login flow`() = runTest {

        try {
            Supabase.login(email = "mattia.cavina2@studio.unibo.it", pass = "psw123")

            // Verifichiamo che la sessione utente non sia nulla dopo il login
            val sessioneAttuale = Supabase.supabase.auth.currentSessionOrNull()
            Assert.assertNotNull("La sessione dovrebbe esistere dopo il login", sessioneAttuale)

        } catch (e: Exception) {
            Assert.fail("Il login è fallito: ${e.message}")
        }
    }
}