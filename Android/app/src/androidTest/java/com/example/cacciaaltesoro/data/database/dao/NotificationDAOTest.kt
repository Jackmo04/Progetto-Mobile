package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class NotificationDAOTest {

    val conn = Supabase()

    val dao: NotificationDAO
        get() = NotificationDAO(conn.supabase)



    @Test
    fun `test recupero tutti le notifiche` () = runTest {
        conn.login("mattia.cavina2@studio.unibo.it" , "psw123")

        val result = dao.getAllNotify()

        Assert.assertNotNull(result)
        Assert.assertEquals("PROVA1",result?.firstOrNull()?.message)
        Assert.assertNotNull(result?.firstOrNull()?.userDTO)
    }
}