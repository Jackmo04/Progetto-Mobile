package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.ui.database.DAO.TableName
import com.example.cacciaaltesoro.ui.database.Supabase
import com.example.cacciaaltesoro.ui.database.api.Utente
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Test to connect supabase with application
 */
class UtenteDAOTest {

    @Test
    fun `test recupero utente tramite username` () = runTest{
        val conn = Supabase.supabase;
        val username = "mattia.cavina2@studio.unibo.it"

        class UtenteDAO {
            private val conn = Supabase.supabase

            suspend fun getUserByUsername(username: String): Utente? {
                return try {
                    conn.from(TableName.UTENTI.tableName).select {
                        filter {
                            Utente::ute_username eq username
                        }
                    }.decodeSingleOrNull<Utente>()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        val user = UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")

        assertEquals(username,
            UtenteDAO().getUserByUsername("mattia.cavina2@studio.unibo.it")?.ute_username
        )
    }
}