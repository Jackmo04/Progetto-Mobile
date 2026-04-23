package com.example.cacciaaltesoro

import androidx.test.espresso.util.filter
import com.example.cacciaaltesoro.ui.database.DAO.PartitaDAO
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