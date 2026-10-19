package pe.edu.cibertec.tecnomantApp
import android.app.Application
class TecnoMantApplication : Application() {
    override fun onCreate() { super.onCreate(); instancia=this }
    companion object { lateinit var instancia: TecnoMantApplication; private set }
}
