package pe.edu.cibertec.tecnomantApp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR5.5 (todos con resultado), FR5.6 (RN-01) y FR5.7 (RN-08, estado general). */
class ReglasInspeccionTest {

    @Test
    fun itemSinResultado_noPasaLaValidacion() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "")
        )

        assertFalse(ReglasInspeccion.todosTienenResultado(items))
    }

    @Test
    fun todosConResultado_pasanLaValidacion() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Regular"),
            ItemInspeccion("Puertas", "Malo", "Puerta descuadrada")
        )

        assertTrue(ReglasInspeccion.todosTienenResultado(items))
    }

    @Test
    fun maloSinObservacion_noPasa() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Malo", "")
        )

        val faltantes = ReglasInspeccion.malosSinObservacion(items)

        assertEquals(listOf("Ruidos"), faltantes.map { it.nombreItem })
    }

    @Test
    fun maloConSoloEspacios_noPasa() {
        val items = listOf(ItemInspeccion("Fugas", "Malo", "   "))

        val faltantes = ReglasInspeccion.malosSinObservacion(items)

        assertEquals(1, faltantes.size)
    }

    @Test
    fun maloConObservacion_pasa() {
        val items = listOf(
            ItemInspeccion("Fugas", "Malo", "Fuga de aceite en la base"),
            ItemInspeccion("Filtros", "Regular", "")
        )

        assertTrue(ReglasInspeccion.malosSinObservacion(items).isEmpty())
    }

    @Test
    fun estadoGeneral_esMaloSiAlgunItemEsMalo() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Regular"),
            ItemInspeccion("Puertas", "Malo", "No cierra")
        )

        assertEquals("Malo", ReglasInspeccion.calcularEstadoGeneral(items))
    }

    @Test
    fun estadoGeneral_esRegularSiNoHayMaloYAlgunoEsRegular() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Regular")
        )

        assertEquals("Regular", ReglasInspeccion.calcularEstadoGeneral(items))
    }

    @Test
    fun estadoGeneral_esBienSiTodosSonBien() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Bien")
        )

        assertEquals("Bien", ReglasInspeccion.calcularEstadoGeneral(items))
    }

    @Test
    fun maloSinFotos_faltaFoto() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Malo", "Ruido fuerte")
        )

        assertTrue(ReglasInspeccion.faltaFoto(items, 0))
    }

    @Test
    fun maloConUnaFoto_noFaltaFoto() {
        val items = listOf(ItemInspeccion("Ruidos", "Malo", "Ruido fuerte"))

        assertFalse(ReglasInspeccion.faltaFoto(items, 1))
    }

    @Test
    fun sinItemsMalos_laFotoEsOpcional() {
        val items = listOf(
            ItemInspeccion("Funcionamiento", "Bien"),
            ItemInspeccion("Ruidos", "Regular")
        )

        assertFalse(ReglasInspeccion.faltaFoto(items, 0))
    }

    @Test
    fun variosMalosSinFotos_faltaFoto() {
        val items = listOf(
            ItemInspeccion("Ruidos", "Malo", "Ruido fuerte"),
            ItemInspeccion("Puertas", "Malo", "No cierra")
        )

        assertTrue(ReglasInspeccion.faltaFoto(items, 0))
    }

}
