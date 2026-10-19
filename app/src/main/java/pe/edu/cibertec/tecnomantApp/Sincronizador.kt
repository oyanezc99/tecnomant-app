package pe.edu.cibertec.tecnomantApp

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Base64
import java.io.File
import java.util.concurrent.Executors
import pe.edu.cibertec.tecnomantApp.db.InspeccionDao
import pe.edu.cibertec.tecnomantApp.retrofit.ClienteTecnoMantRetrofit
import pe.edu.cibertec.tecnomantApp.retrofit.request.*

object Sincronizador {
    private const val JOB_ID=5080
    private val cola=Executors.newSingleThreadExecutor()
    fun programar(contexto: Context) {
        val tarea=JobInfo.Builder(JOB_ID,ComponentName(contexto,SincronizacionJobService::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setBackoffCriteria(30000,JobInfo.BACKOFF_POLICY_EXPONENTIAL)
            .setPersisted(true).build()
        contexto.getSystemService(JobScheduler::class.java).schedule(tarea)
    }
    fun ejecutar(contexto: Context, terminar: (Boolean,String)->Unit) {
        val contextoApp=contexto.applicationContext
        cola.execute {
            var exito=true
            var mensaje="Sincronización completada"
            try {
                if(Sesion.token(contextoApp).isBlank()) throw IllegalStateException("Inicia sesión para sincronizar")
                val dao=InspeccionDao(contextoApp)
                val resultado=dao.listarBorradores()
                if(!resultado.ok) throw IllegalStateException("No se pudo leer la cola local")
                for(borrador in ColaSincronizacion.pendientes(resultado.dato?:emptyList())) {
                    val inspeccion=dao.obtenerPorOrden(borrador.idOrden).dato ?: throw IllegalStateException("No se pudo leer la inspección")
                    val rutas=dao.listarEvidencias(inspeccion.id)
                    if(!rutas.ok) throw IllegalStateException("No se pudieron leer las fotos")
                    val fotos=(rutas.dato?:emptyList()).map { ruta ->
                        val archivo=File(ruta)
                        if(!archivo.exists() || archivo.length()>2*1024*1024) throw IllegalStateException("Revisa las fotos del borrador")
                        EvidenciaRequest(archivo.name,Base64.encodeToString(archivo.readBytes(),Base64.NO_WRAP))
                    }
                    val pedido=InspeccionRequest(inspeccion.idOrden,inspeccion.fechaHora,inspeccion.observaciones,inspeccion.items.map { ItemInspeccionRequest(it.nombreItem,it.resultado,it.observacion) },fotos)
                    val respuesta=ClienteTecnoMantRetrofit.retrofitTecnoMantService.sincronizarInspeccion(pedido).execute()
                    if(!respuesta.isSuccessful) throw IllegalStateException(if(respuesta.code()==401) "La sesión venció; inicia sesión nuevamente" else "No se pudo sincronizar (HTTP "+respuesta.code()+"). El borrador se conserva")
                    if(!dao.marcarSincronizada(inspeccion.id)) throw IllegalStateException("No se pudo confirmar la sincronización local")
                }
            } catch(e: Exception) { exito=false; mensaje=e.message?:"Sin conexión; se reintentará" }
            terminar(exito,mensaje)
        }
    }
}
