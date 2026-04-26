package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.DAO.NotificheDAO
import com.example.cacciaaltesoro.data.database.DAO.UtenteDAO
import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class NotificheDAOTest {

    val dao: NotificheDAO
        get() = NotificheDAO()



    @Test
    fun `test recupero tutti le notifiche` () = runTest {
        Supabase.login("mattia.cavina2@studio.unibo.it" , "psw123")

        val result = dao.getAllNotify();

        Assert.assertNotNull(result)
        Assert.assertEquals("PROVA1",result?.firstOrNull()?.not_messaggio)
        Assert.assertNotNull(result?.firstOrNull()?.utente)
    }
}