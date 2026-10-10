package pe.edu.cibertec.tecnomantApp.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import pe.edu.cibertec.tecnomantApp.Orden
import pe.edu.cibertec.tecnomantApp.ReglasOrden

/** Acceso a la tabla local `orden`. Ninguna pantalla ejecuta SQL directamente. */
class OrdenDao(context: Context) {

    private val helper = TecnoMantDbHelper.obtener(context)

    /** Todas las órdenes guardadas, por fecha programada. */
    fun listar(): ResultadoDb<List<Orden>> {
        return try {
            val cursor = helper.readableDatabase.rawQuery(
                "SELECT $COLUMNAS FROM orden ORDER BY fechaProgramada, idOrden", null
            )
            ResultadoDb(true, leerOrdenes(cursor))
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudieron listar las órdenes", e)
            ResultadoDb(false, null)
        }
    }

    /** BUSCAR: filtra con LIKE por cliente, tipo de equipo, código de equipo y estado. */
    fun buscar(texto: String): ResultadoDb<List<Orden>> {
        return try {
            val patron = "%" + escaparLike(texto) + "%"
            val cursor = helper.readableDatabase.rawQuery(
                "SELECT $COLUMNAS FROM orden " +
                    "WHERE cliente LIKE ? ESCAPE '\\' " +
                    "OR equipoTipo LIKE ? ESCAPE '\\' " +
                    "OR equipoCodigo LIKE ? ESCAPE '\\' " +
                    "OR estado LIKE ? ESCAPE '\\' " +
                    "ORDER BY fechaProgramada, idOrden",
                arrayOf(patron, patron, patron, patron)
            )
            ResultadoDb(true, leerOrdenes(cursor))
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudieron buscar las órdenes", e)
            ResultadoDb(false, null)
        }
    }

    /** Una orden por su id. `dato` es null si la orden no existe. */
    fun obtener(idOrden: Int): ResultadoDb<Orden> {
        return try {
            val cursor = helper.readableDatabase.rawQuery(
                "SELECT $COLUMNAS FROM orden WHERE idOrden = ?",
                arrayOf(idOrden.toString())
            )
            ResultadoDb(true, leerOrdenes(cursor).firstOrNull())
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo leer la orden $idOrden", e)
            ResultadoDb(false, null)
        }
    }

    /**
     * Guarda lo que devolvió la API al pulsar "Actualizar órdenes", todo en una transacción:
     * - inserta o actualiza cada orden por idOrden (no se duplican);
     * - una orden con inspección guardada conserva su estado local;
     * - borra las órdenes locales que ya no vienen y no tienen inspección (ReglasOrden).
     * Devuelve false si la base de datos falló; en ese caso no se guarda nada.
     */
    fun guardarOrdenesDelServidor(ordenes: List<Orden>): Boolean {
        return try {
            val db = helper.writableDatabase
            db.beginTransaction()
            try {
                val idsLocales = leerIds(db, "SELECT idOrden FROM orden")
                val idsConInspeccion = leerIds(db, "SELECT idOrden FROM inspeccion")

                for (orden in ordenes) {
                    val conservarEstado = idsConInspeccion.contains(orden.idOrden)
                    insertarOActualizar(db, orden, idsLocales.contains(orden.idOrden), conservarEstado)
                }

                val idsABorrar = ReglasOrden.idsABorrar(
                    idsLocales, ordenes.map { it.idOrden }, idsConInspeccion
                )
                for (id in idsABorrar) {
                    db.delete("orden", "idOrden = ?", arrayOf(id.toString()))
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            true
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudieron guardar las órdenes del servidor", e)
            false
        }
    }

    /** Cambia el estado local de una orden. */
    fun cambiarEstado(idOrden: Int, estado: String): Boolean {
        return try {
            val valores = ContentValues()
            valores.put("estado", estado)
            helper.writableDatabase.update("orden", valores, "idOrden = ?", arrayOf(idOrden.toString()))
            true
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo cambiar el estado de la orden $idOrden", e)
            false
        }
    }

    private fun insertarOActualizar(
        db: SQLiteDatabase,
        orden: Orden,
        yaExiste: Boolean,
        conservarEstado: Boolean
    ) {
        val valores = ContentValues()
        valores.put("cliente", orden.cliente)
        valores.put("direccion", orden.direccion)
        valores.put("equipoTipo", orden.equipoTipo)
        valores.put("equipoCodigo", orden.equipoCodigo)
        valores.put("ubicacion", orden.ubicacion)
        valores.put("fechaProgramada", orden.fechaProgramada)
        if (!yaExiste) {
            valores.put("idOrden", orden.idOrden)
            valores.put("estado", orden.estado)
            db.insertOrThrow("orden", null, valores)
            return
        }
        if (!conservarEstado) {
            valores.put("estado", orden.estado)
        }
        db.update("orden", valores, "idOrden = ?", arrayOf(orden.idOrden.toString()))
    }

    private fun leerIds(db: SQLiteDatabase, sql: String): List<Int> {
        val ids = ArrayList<Int>()
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getInt(0))
            }
        }
        return ids
    }

    private fun leerOrdenes(cursor: Cursor): List<Orden> {
        val lista = ArrayList<Orden>()
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Orden(
                        idOrden = it.getInt(0),
                        cliente = it.getString(1) ?: "",
                        direccion = it.getString(2) ?: "",
                        equipoTipo = it.getString(3) ?: "",
                        equipoCodigo = it.getString(4) ?: "",
                        ubicacion = it.getString(5) ?: "",
                        fechaProgramada = it.getString(6) ?: "",
                        estado = it.getString(7) ?: ""
                    )
                )
            }
        }
        return lista
    }

    /** Evita que los caracteres % y _ que escriba el usuario se tomen como comodines de LIKE. */
    private fun escaparLike(texto: String): String {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    }

    companion object {
        private const val TAG = "OrdenDao"
        private const val COLUMNAS =
            "idOrden, cliente, direccion, equipoTipo, equipoCodigo, ubicacion, fechaProgramada, estado"
    }
}
