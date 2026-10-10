package pe.edu.cibertec.tecnomantApp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR3.4: qué órdenes locales se borran al pulsar "Actualizar órdenes". */
class ReglasOrdenTest {

    @Test
    fun seBorraLaOrdenQueNoVieneYNoTieneInspeccion() {
        val idsABorrar = ReglasOrden.idsABorrar(
            idsLocales = listOf(1, 2, 3),
            idsServidor = listOf(1, 3),
            idsConInspeccion = emptyList()
        )

        assertEquals(listOf(2), idsABorrar)
    }

    @Test
    fun noSeBorraLaOrdenQueTieneInspeccion() {
        val idsABorrar = ReglasOrden.idsABorrar(
            idsLocales = listOf(1, 2, 3),
            idsServidor = listOf(1),
            idsConInspeccion = listOf(2)
        )

        assertFalse(idsABorrar.contains(2))
        assertEquals(listOf(3), idsABorrar)
    }

    @Test
    fun noSeBorraLaOrdenQueSiViene() {
        val idsABorrar = ReglasOrden.idsABorrar(
            idsLocales = listOf(1, 2),
            idsServidor = listOf(1, 2, 5),
            idsConInspeccion = emptyList()
        )

        assertTrue(idsABorrar.isEmpty())
    }
}
