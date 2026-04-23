package com.example.cacciaaltesoro

import androidx.test.espresso.util.filter
import com.example.cacciaaltesoro.ui.database.DAO.PartitaDAO
import com.example.cacciaaltesoro.ui.database.DAO.TagDAO
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
class TagDAOTest {

    val dao: TagDAO
        get() = TagDAO()


    @Test
    fun `test tag by id` () = runTest {
        Supabase.login("mattia.cavina2@studio.unibo.it" , "psw123")

        val result =dao.getTagByID(1)

        Assert.assertNotNull(result)
        Assert.assertEquals(
            "HASH_1_1_f48cdcd0",
            result?.tag_hash
        )
    }


}