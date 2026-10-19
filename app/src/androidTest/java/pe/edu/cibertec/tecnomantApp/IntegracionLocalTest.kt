package pe.edu.cibertec.tecnomantApp

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import pe.edu.cibertec.tecnomantApp.db.*
import pe.edu.cibertec.tecnomantApp.retrofit.ClienteTecnoMantRetrofit
import pe.edu.cibertec.tecnomantApp.retrofit.request.LoginRequest

/** Prueba optativa contra una API local y un técnico de integración sin datos reales. */
@RunWith(AndroidJUnit4::class)
class IntegracionLocalTest {
    @Test fun descargaGuardaEnSQLiteEnviaFotosYBloqueaEdicion() {
        val args=InstrumentationRegistry.getArguments()
        val usuario=args.getString("usuario")?:""
        val clave=args.getString("clave")?:""
        assumeTrue("Requiere usuario de pruebas de la API local",usuario.startsWith("int-"))
        val contexto=InstrumentationRegistry.getInstrumentation().targetContext
        val servicio=ClienteTecnoMantRetrofit.retrofitTecnoMantService
        val acceso=servicio.iniciarSesion(LoginRequest(usuario,clave)).execute()
        assertTrue("Login HTTP "+acceso.code(),acceso.isSuccessful)
        val sesion=acceso.body()!!
        Sesion.guardar(contexto,sesion.idTecnico!!,sesion.nombre?:"",sesion.token?:"")
        val descargadas=servicio.obtenerOrdenes(sesion.idTecnico).execute()
        assertTrue(descargadas.isSuccessful)
        val ordenes=descargadas.body()!!.map { Orden(it.idOrden!!,it.cliente?:"",it.direccion?:"",it.equipoTipo?:"",it.equipoCodigo?:"",it.ubicacion?:"",it.fechaProgramada?:"",it.estado?:"") }
        assertTrue("Se necesitan dos órdenes de integración pendientes",ordenes.size>=2)
        val dao=InspeccionDao(contexto)
        assertTrue(OrdenDao(contexto).guardarOrdenesDelServidor(ordenes))
        val primera=ordenes[0]
        val segunda=ordenes[1]
        for(orden in listOf(primera,segunda)) {
            val previa=dao.obtenerPorOrden(orden.idOrden).dato
            if(previa!=null && !previa.sincronizada) assertTrue(dao.borrar(previa.id,orden.idOrden))
        }
        val foto=ArchivosEvidencia.crearArchivo(contexto)
        foto.outputStream().use { Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG,80,it) }
        val items=Checklist.itemsPara(primera.equipoTipo).map { ItemInspeccion(it,if(it=="Ruidos") "Malo" else "Bien",if(it=="Ruidos") "Ruido simulado en integración Android" else "") }
        // Se guarda primero la fecha más reciente para comprobar que la cola ordena por fecha.
        assertTrue(dao.insertar(segunda.idOrden,"2026-10-19T12:31:00","Prueba Android FIFO 2", "Malo", items,listOf(foto.absolutePath)))
        assertTrue(dao.insertar(primera.idOrden,"2026-10-19T12:30:00","Prueba Android FIFO 1", "Malo", items,listOf(foto.absolutePath)))
        assertFalse(dao.obtenerPorOrden(primera.idOrden).dato!!.sincronizada)
        // En el emulador de prueba se corta la conectividad y se comprueba que SQLite conserva el trabajo.
        val ui=InstrumentationRegistry.getInstrumentation().uiAutomation
        fun red(comando:String) { ui.executeShellCommand(comando).use { android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes() } }
        val conectividad=contexto.getSystemService(android.net.ConnectivityManager::class.java)
        try {
            red("svc wifi disable")
            red("svc data disable")
            val limiteSinRed=System.currentTimeMillis()+10000
            while(conectividad.activeNetwork!=null && System.currentTimeMillis()<limiteSinRed) Thread.sleep(100)
            assertNull("La prueba requiere que el emulador esté sin red",conectividad.activeNetwork)
            val fallo=CountDownLatch(1)
            var exitoInesperado=true
            Sincronizador.ejecutar(contexto) { ok,_ -> exitoInesperado=ok; fallo.countDown() }
            assertTrue(fallo.await(90,TimeUnit.SECONDS))
            assertFalse(exitoInesperado)
            assertFalse(dao.obtenerPorOrden(primera.idOrden).dato!!.sincronizada)
            assertTrue(foto.exists())
        } finally {
            red("svc wifi enable")
            red("svc data enable")
        }
        val limiteConRed=System.currentTimeMillis()+20000
        while(conectividad.activeNetwork==null && System.currentTimeMillis()<limiteConRed) Thread.sleep(100)
        // El sistema ejecuta el trabajo al tener red; no se fuerza la llamada manual.
        Sincronizador.programar(contexto)
        val limite=System.currentTimeMillis()+60000
        while(System.currentTimeMillis()<limite && dao.obtenerPorOrden(segunda.idOrden).dato?.sincronizada!=true) Thread.sleep(250)
        val guardada=dao.obtenerPorOrden(primera.idOrden).dato!!
        assertTrue("JobScheduler debe enviar la cola cuando hay red",guardada.sincronizada)
        assertEquals("Completada",OrdenDao(contexto).obtener(primera.idOrden).dato!!.estado)
        assertTrue(dao.obtenerPorOrden(segunda.idOrden).dato!!.sincronizada)
        assertFalse("La inspección sincronizada es inmutable",dao.actualizar(guardada.id,primera.idOrden,guardada.fechaHora,"No modificar","Bien",items,listOf(foto.absolutePath)))
        assertFalse("No se permite borrar una inspección enviada",dao.borrar(guardada.id,primera.idOrden))
        val restantes=servicio.obtenerOrdenes(sesion.idTecnico).execute().body()!!
        assertFalse(restantes.any { it.idOrden==primera.idOrden || it.idOrden==segunda.idOrden })
    }
}
