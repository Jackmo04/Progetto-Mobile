package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.ui.database.DAO.TableName
import com.example.cacciaaltesoro.ui.database.DAO.UtenteDAO
import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class UtenteDAOTest {

    @Test
    fun `test recupero utente tramite username` () = runTest {

        val conn = Supabase.supabase;
        Supabase.login("mattia.cavina2@studio.unibo.it","psw123")
        val username = "mattia.cavina2@studio.unibo.it"

        val user = UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")

        Assert.assertEquals(
            username,
            UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")?.ute_username
        )
    }

    @Test
    fun `test recupero tutti gli utenti` () = runTest {

        val username = "mattia.cavina2@studio.unibo.it"

        val result = UtenteDAO().getAllUsere();

        Assert.assertNotNull(result)
    }
}