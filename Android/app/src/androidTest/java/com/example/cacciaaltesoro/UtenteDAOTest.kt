package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.DAO.UtenteDAO
import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class UtenteDAOTest {

    val conn = Supabase()
    val dao: UtenteDAO
        get() = UtenteDAO(conn.supabase)


    @Before
    fun setup() = runTest {
        conn.login("mattia.cavina2@studio.unibo.it", "psw123")
    }

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

        val result = dao.getAllUser();

        Assert.assertNotNull(result)
    }

    @Test
    fun `test recupero utente con partite` () = runTest {
        val username = "mattia.cavina2@studio.unibo.it"

        val result = dao.getAllUserSMatch(username)

        Assert.assertNotNull(result)
        Assert.assertEquals(username, result?.ute_username)

        Assert.assertTrue( result?.partite?.isNotEmpty() == true)
    }

    @Test
    fun `test recupero tag raccolti da utente in partita` () = runTest {
        val username = "mattia.cavina2@studio.unibo.it"
        val partitaId = 1

        val result = dao.getAllUserSCatchesTag(username, partitaId)

        Assert.assertNotNull(result)
        Assert.assertEquals(username, result?.ute_username)
        result?.tags?.forEach { tag ->
            Assert.assertEquals(partitaId, tag.tag_partita)
        }
    }
}