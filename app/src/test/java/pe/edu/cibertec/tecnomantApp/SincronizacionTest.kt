package pe.edu.cibertec.tecnomantApp
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import pe.edu.cibertec.tecnomantApp.retrofit.request.*
class SincronizacionTest {
    @Test fun enviaFechaYFotosConElContratoDeLaApi() {
        val pedido=InspeccionRequest(10,"2026-10-19T10:00:00","Prueba",listOf(ItemInspeccionRequest("Ruidos","Malo","Ruido")),listOf(EvidenciaRequest("foto.jpg","YWJj")))
        val json=Gson().toJsonTree(pedido).asJsonObject
        assertTrue(json.has("fechaHoraReal")); assertFalse(json.has("fechaHora"))
        assertEquals("YWJj",json.getAsJsonArray("evidencias")[0].asJsonObject.get("fotoBase64").asString)
    }
    @Test fun enviaPendientesEnOrdenCronologicoEstable() {
        fun b(id:Long,fecha:String,sincronizada:Boolean=false)=Borrador(id,id.toInt(),"EQ","Cliente",fecha,sincronizada)
        val cola=ColaSincronizacion.pendientes(listOf(b(3,"2026-10-19T10:00:00"),b(2,"2026-10-18T10:00:00"),b(1,"2026-10-18T10:00:00"),b(4,"2026-10-17T10:00:00",true)))
        assertEquals(listOf(1L,2L,3L),cola.map{it.idInspeccion})
    }
}
