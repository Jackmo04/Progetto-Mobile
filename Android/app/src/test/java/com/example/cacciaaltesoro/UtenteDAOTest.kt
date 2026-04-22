package com.example.cacciaaltesoro

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.cacciaaltesoro.ui.database.DAO.UtenteDAO
import com.example.cacciaaltesoro.ui.database.Supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*

/**
 * Test to connect supabase with application
 */
class UtenteDAOTest {

    @Test
    fun `test recupero utente tramite username` () = runTest{
        val conn = Supabase.supabase;
        val username = "mattia.cavina2@studio.unibo.it"

        val user = UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")

        assertEquals(username,
            UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")?.ute_username
        )
    }
}