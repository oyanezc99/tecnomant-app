package pe.edu.cibertec.tecnomantApp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Base de datos local de TecnoMant (SQLite, sin Room).
 * Crea las cuatro tablas de la app. La tabla `evidencia` queda creada para la
 * versión final (fotos) y no se usa en el avance.
 */
class TecnoMantDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE orden (" +
                "idOrden INTEGER PRIMARY KEY, " +
                "cliente TEXT, " +
                "direccion TEXT, " +
                "equipoTipo TEXT, " +
                "equipoCodigo TEXT, " +
                "ubicacion TEXT, " +
                "fechaProgramada TEXT, " +
                "estado TEXT)"
        )
        // RN-06: una orden tiene una sola inspección (idOrden es UNIQUE).
        db.execSQL(
            "CREATE TABLE inspeccion (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idOrden INTEGER UNIQUE, " +
                "fechaHora TEXT, " +
                "observaciones TEXT, " +
                "estadoGeneral TEXT, " +
                "sincronizada INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE item (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idInspeccion INTEGER, " +
                "nombreItem TEXT, " +
                "resultado TEXT, " +
                "observacion TEXT)"
        )
        db.execSQL(
            "CREATE TABLE evidencia (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idInspeccion INTEGER, " +
                "rutaFoto TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // La base está en su versión 1: todavía no hay cambios de estructura que aplicar.
        // Cuando exista la versión 2, aquí van sus ALTER TABLE para no perder los datos.
    }

    companion object {
        const val NOMBRE_BD = "tecnomant.db"
        const val VERSION_BD = 1

        private var instancia: TecnoMantDbHelper? = null

        /** Devuelve siempre el mismo helper, para que toda la app use una sola conexión. */
        @Synchronized
        fun obtener(context: Context): TecnoMantDbHelper {
            val actual = instancia
            if (actual != null) return actual
            val nuevo = TecnoMantDbHelper(context.applicationContext)
            instancia = nuevo
            return nuevo
        }
    }
}
