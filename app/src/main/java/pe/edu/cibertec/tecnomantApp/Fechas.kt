package pe.edu.cibertec.tecnomantApp

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formato de fechas de la app.
 * Se guardan como llegan de la API (yyyy-MM-ddTHH:mm:ss) y se muestran como dd/MM/yyyy HH:mm.
 */
object Fechas {
    private const val FORMATO_GUARDADO = "yyyy-MM-dd'T'HH:mm:ss"
    private const val FORMATO_PANTALLA = "dd/MM/yyyy HH:mm"

    /** Fecha y hora actual en el formato en que se guarda. */
    fun ahora(): String {
        return SimpleDateFormat(FORMATO_GUARDADO, Locale.US).format(Date())
    }
    /** Convierte una fecha guardada al formato de pantalla. Si no se puede leer, la devuelve tal cual. */
    fun mostrar(fechaGuardada: String): String {
        return try {
            val formatoGuardado = SimpleDateFormat(FORMATO_GUARDADO, Locale.US)
            formatoGuardado.isLenient = false
            val fecha = formatoGuardado.parse(fechaGuardada) ?: return fechaGuardada
            SimpleDateFormat(FORMATO_PANTALLA, Locale.US).format(fecha)
        } catch (e: ParseException) {
            fechaGuardada
        }
    }
}
