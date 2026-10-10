package pe.edu.cibertec.tecnomantApp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** S6: las fechas se guardan como llegan de la API y se muestran como dd/MM/yyyy HH:mm. */
class FechasTest {

    @Test
    fun mostrar_convierteLaFechaDeLaApiAlFormatoDePantalla() {
        assertEquals("10/10/2026 09:00", Fechas.mostrar("2026-10-10T09:00:00"))
    }

    @Test
    fun mostrar_devuelveElTextoTalCualSiNoEsUnaFecha() {
        assertEquals("sin fecha", Fechas.mostrar("sin fecha"))
        assertEquals("", Fechas.mostrar(""))
    }

    @Test
    fun ahora_tieneElFormatoEnQueSeGuarda() {
        val patron = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}""")

        assertTrue(patron.matches(Fechas.ahora()))
    }
}
