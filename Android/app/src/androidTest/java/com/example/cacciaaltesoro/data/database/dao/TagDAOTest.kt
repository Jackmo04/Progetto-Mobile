package com.example.cacciaaltesoro.data.database.dao

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
    fun `test getTagByPosMatch returns correct tag`() = runTest {
        val tagPos = 1
        val partita = 1
        val result = dao.getTagByPosMatch(tagPos , partita)

        Assert.assertNotNull(result)
        Assert.assertEquals(tagPos, result?.number)
        Assert.assertEquals(partita, result?.eventId)
    }

    @Test
    fun `test getTagById returns correct tag`() = runTest {
        val tagPos = 1
        val partita = 1
        val tag = dao.getTagByPosMatch(tagPos , partita)
        Assert.assertNotNull(tag)

        val result = dao.getTagByUUID(tag!!.id)

        Assert.assertNotNull(result)
        Assert.assertEquals(tag.id, result?.id)
        Assert.assertEquals(tagPos, result?.number)
        Assert.assertEquals(partita, result?.eventId)
    }


}
