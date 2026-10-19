package pe.edu.cibertec.tecnomantApp
import android.app.job.JobService
import android.app.job.JobParameters
class SincronizacionJobService : JobService() {
    @Volatile private var detenido=false
    override fun onStartJob(params: JobParameters): Boolean {
        detenido=false
        Sincronizador.ejecutar(applicationContext) { ok,_ -> if(!detenido) jobFinished(params,!ok) }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { detenido=true; return true }
}
