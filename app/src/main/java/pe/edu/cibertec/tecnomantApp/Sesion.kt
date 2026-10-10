package pe.edu.cibertec.tecnomantApp

import android.content.Context

/**
 * Sesión del técnico en SharedPreferences (archivo "sesion").
 * Solo se guardan idTecnico y nombre; la clave nunca se guarda en el celular.
 */
object Sesion {

    private const val ARCHIVO = "sesion"
    private const val CLAVE_ID_TECNICO = "idTecnico"
    private const val CLAVE_NOMBRE = "nombre"
    private const val SIN_SESION = -1

    fun guardar(context: Context, idTecnico: Int, nombre: String) {
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit()
            .putInt(CLAVE_ID_TECNICO, idTecnico)
            .putString(CLAVE_NOMBRE, nombre)
            .apply()
    }

    fun haySesion(context: Context): Boolean {
        return idTecnico(context) != SIN_SESION
    }

    /** Id del técnico con sesión iniciada, o -1 si no hay sesión. */
    fun idTecnico(context: Context): Int {
        return context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getInt(CLAVE_ID_TECNICO, SIN_SESION)
    }

    fun nombre(context: Context): String {
        return context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString(CLAVE_NOMBRE, "") ?: ""
    }

    /** Cerrar sesión: borra solo la sesión. Los datos de SQLite se conservan. */
    fun borrar(context: Context) {
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit()
            .remove(CLAVE_ID_TECNICO)
            .remove(CLAVE_NOMBRE)
            .apply()
    }
}
