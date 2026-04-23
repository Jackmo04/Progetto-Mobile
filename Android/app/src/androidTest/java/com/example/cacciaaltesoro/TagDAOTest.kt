package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.DAO.TagDAO
import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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