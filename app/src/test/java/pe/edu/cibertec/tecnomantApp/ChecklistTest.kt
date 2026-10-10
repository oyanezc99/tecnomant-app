package pe.edu.cibertec.tecnomantApp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR5.1: el checklist depende del tipo de equipo. */
class ChecklistTest {

    @Test
    fun ascensor_devuelveSusCincoItems() {
        val items = Checklist.itemsPara("Ascensor")

        assertEquals(
            listOf("Funcionamiento", "Ruidos", "Puertas", "Desgaste de cables", "Iluminación"),
            items
        )
    }

    @Test
    fun aireAcondicionado_devuelveSusCincoItems() {
        val items = Checklist.itemsPara("Aire acondicionado")

        assertEquals(
            listOf("Funcionamiento", "Ruidos", "Fugas", "Filtros", "Desgaste"),
            items
        )
    }

    @Test
    fun activoIndustrial_devuelveSusCincoItems() {
        val items = Checklist.itemsPara("Activo industrial")

        assertEquals(
            listOf("Funcionamiento", "Ruidos", "Fugas", "Desgaste", "Protecciones"),
            items
        )
    }

    @Test
    fun tipoDesconocido_devuelveListaVacia() {
        assertTrue(Checklist.itemsPara("Montacargas").isEmpty())
        assertTrue(Checklist.itemsPara("").isEmpty())
    }
}
