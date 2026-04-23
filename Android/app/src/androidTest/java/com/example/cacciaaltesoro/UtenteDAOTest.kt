package com.example.cacciaaltesoro

import androidx.test.espresso.util.filter
import com.example.cacciaaltesoro.ui.database.DAO.UtenteDAO
import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class UtenteDAOTest {

    val dao: UtenteDAO
        get() = UtenteDAO()


    @Test
    fun `test recupero utente tramite username` () = runTest {
        val username = "mattia.cavina2@studio.unibo.it"

        val user =dao.getUserByUsername(username)

        Assert.assertNotNull(user)
        Assert.assertEquals(
            username,
            user?.ute_username
        )
    }


    @Test
    fun `test recupero tutti gli utenti` () = runTest {

        val result = dao.getAllUsere();

        Assert.assertNotNull(result)
    }
}