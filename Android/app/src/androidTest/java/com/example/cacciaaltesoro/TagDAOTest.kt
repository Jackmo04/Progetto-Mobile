package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.DAO.TagDAO
import com.example.cacciaaltesoro.data.database.Supabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Instrumented test for TagDAO.
 */
class TagDAOTest {

    val conn = Supabase()
    private val dao = TagDAO(conn.supabase)

    @Before
    fun setup() = runTest {
        conn.login("mattia.cavina2@studio.unibo.it", "psw123")
    }

    @Test
    fun `test getTagByID returns correct tag`() = runTest {
        val tagId = 1
        val result = dao.getTagByID(tagId)

        Assert.assertNotNull(result)
        Assert.assertEquals(tagId, result?.tag_id)
        Assert.assertNotNull(result?.partita)
    }


}
