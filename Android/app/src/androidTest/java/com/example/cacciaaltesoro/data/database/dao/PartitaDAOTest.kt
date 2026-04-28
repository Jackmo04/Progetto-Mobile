package com.example.cacciaaltesoro.data.database.dao

import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class PartitaDAOTest {

    val conn = Supabase()
    val dao: PartitaDAO
        get() = PartitaDAO(conn.supabase)


    @Test
    fun `test partita by id` () = runTest {
        conn.login("mattia.cavina2@studio.unibo.it" , "psw123")

        val result = dao.getPartitaByID(1)

        Assert.assertNotNull(result)
        Assert.assertEquals(
            "PARTITA1",
            result?.name
        )

       Assert.assertEquals("mattia.cavina2@studio.unibo.it" , result?.user?.username)
    }


}