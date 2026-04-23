package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.DAO.PartitaDAO
import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class PartitaDAOTest {

    val dao: PartitaDAO
        get() = PartitaDAO()


    @Test
    fun `test partita by id` () = runTest {
        Supabase.login("mattia.cavina2@studio.unibo.it" , "psw123")

        val result =dao.getPartitaByID(1)

        Assert.assertNotNull(result)
        Assert.assertEquals(
            "PARTITA1",
            result?.par_nome
        )
    }


}