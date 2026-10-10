package pe.edu.cibertec.tecnomantApp.db

import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import pe.edu.cibertec.tecnomantApp.Borrador
import pe.edu.cibertec.tecnomantApp.Inspeccion
import pe.edu.cibertec.tecnomantApp.ItemInspeccion
import pe.edu.cibertec.tecnomantApp.ReglasOrden

/**
 * Acceso a las tablas locales `inspeccion` e `item`.
 * Todo cambio que toca varias filas va dentro de una transacción: o se guarda todo o nada.
 */
class InspeccionDao(context: Context) {

    private val helper = TecnoMantDbHelper.obtener(context)

    /** La inspección de una orden con sus ítems. `dato` es null si la orden no tiene inspección. */
    fun obtenerPorOrden(idOrden: Int): ResultadoDb<Inspeccion> {
        return try {
            val db = helper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT id, idOrden, fechaHora, observaciones, estadoGeneral, sincronizada " +
                    "FROM inspeccion WHERE idOrden = ?",
                arrayOf(idOrden.toString())
            )
            var inspeccion: Inspeccion? = null
            cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(0)
                    inspeccion = Inspeccion(
                        id = id,
                        idOrden = it.getInt(1),
                        fechaHora = it.getString(2) ?: "",
                        observaciones = it.getString(3) ?: "",
                        estadoGeneral = it.getString(4) ?: "",
                        sincronizada = it.getInt(5) == 1,
                        items = leerItems(db, id)
                    )
                }
            }
            ResultadoDb(true, inspeccion)
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo leer la inspección de la orden $idOrden", e)
            ResultadoDb(false, null)
        }
    }

    /**
     * REGISTRAR: inserta la inspección (sincronizada = 0) y una fila por ítem,
     * y pasa la orden a "En proceso" (RN-07).
     */
    fun insertar(
        idOrden: Int,
        fechaHora: String,
        observaciones: String,
        estadoGeneral: String,
        items: List<ItemInspeccion>
    ): Boolean {
        return try {
            val db = helper.writableDatabase
            db.beginTransaction()
            try {
                val valores = ContentValues()
                valores.put("idOrden", idOrden)
                valores.put("fechaHora", fechaHora)
                valores.put("observaciones", observaciones)
                valores.put("estadoGeneral", estadoGeneral)
                valores.put("sincronizada", 0)
                val idInspeccion = db.insertOrThrow("inspeccion", null, valores)

                for (item in items) {
                    db.insertOrThrow("item", null, valoresDeItem(idInspeccion, item))
                }
                cambiarEstadoOrden(db, idOrden, ReglasOrden.EN_PROCESO)

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            true
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo guardar la inspección de la orden $idOrden", e)
            false
        }
    }

    /**
     * MODIFICAR: actualiza la misma inspección y sus ítems; no crea otra.
     * Solo se modifican los borradores (sincronizada = 0, RN-02).
     */
    fun actualizar(
        idInspeccion: Long,
        idOrden: Int,
        fechaHora: String,
        observaciones: String,
        estadoGeneral: String,
        items: List<ItemInspeccion>
    ): Boolean {
        return try {
            val db = helper.writableDatabase
            var actualizada = false
            db.beginTransaction()
            try {
                val valores = ContentValues()
                valores.put("fechaHora", fechaHora)
                valores.put("observaciones", observaciones)
                valores.put("estadoGeneral", estadoGeneral)
                val filas = db.update(
                    "inspeccion", valores,
                    "id = ? AND sincronizada = 0", arrayOf(idInspeccion.toString())
                )
                if (filas == 1) {
                    for (item in items) {
                        actualizarItem(db, idInspeccion, item)
                    }
                    cambiarEstadoOrden(db, idOrden, ReglasOrden.EN_PROCESO)
                    db.setTransactionSuccessful()
                    actualizada = true
                } else {
                    Log.e(TAG, "La inspección $idInspeccion no existe o ya está sincronizada")
                }
            } finally {
                db.endTransaction()
            }
            actualizada
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo actualizar la inspección $idInspeccion", e)
            false
        }
    }

    /**
     * BORRAR: elimina la inspección y sus ítems, y la orden vuelve a "Pendiente".
     * Solo se borran los borradores (sincronizada = 0, RN-02).
     */
    fun borrar(idInspeccion: Long, idOrden: Int): Boolean {
        return try {
            val db = helper.writableDatabase
            var borrada = false
            db.beginTransaction()
            try {
                val filas = db.delete(
                    "inspeccion", "id = ? AND sincronizada = 0", arrayOf(idInspeccion.toString())
                )
                if (filas == 1) {
                    db.delete("item", "idInspeccion = ?", arrayOf(idInspeccion.toString()))
                    cambiarEstadoOrden(db, idOrden, ReglasOrden.PENDIENTE)
                    db.setTransactionSuccessful()
                    borrada = true
                } else {
                    Log.e(TAG, "La inspección $idInspeccion no existe o ya está sincronizada")
                }
            } finally {
                db.endTransaction()
            }
            borrada
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudo borrar la inspección $idInspeccion", e)
            false
        }
    }

    /** Inspecciones guardadas con los datos de su orden, la más reciente primero. */
    fun listarBorradores(): ResultadoDb<List<Borrador>> {
        return try {
            val cursor = helper.readableDatabase.rawQuery(
                "SELECT i.id, i.idOrden, o.equipoCodigo, o.cliente, i.fechaHora, i.sincronizada " +
                    "FROM inspeccion i LEFT JOIN orden o ON o.idOrden = i.idOrden " +
                    "ORDER BY i.fechaHora DESC, i.id DESC",
                null
            )
            val lista = ArrayList<Borrador>()
            cursor.use {
                while (it.moveToNext()) {
                    lista.add(
                        Borrador(
                            idInspeccion = it.getLong(0),
                            idOrden = it.getInt(1),
                            equipoCodigo = it.getString(2) ?: "",
                            cliente = it.getString(3) ?: "",
                            fechaHora = it.getString(4) ?: "",
                            sincronizada = it.getInt(5) == 1
                        )
                    )
                }
            }
            ResultadoDb(true, lista)
        } catch (e: SQLException) {
            Log.e(TAG, "No se pudieron listar los borradores", e)
            ResultadoDb(false, null)
        }
    }

    private fun leerItems(db: SQLiteDatabase, idInspeccion: Long): List<ItemInspeccion> {
        val items = ArrayList<ItemInspeccion>()
        val cursor = db.rawQuery(
            "SELECT nombreItem, resultado, observacion FROM item WHERE idInspeccion = ? ORDER BY id",
            arrayOf(idInspeccion.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    ItemInspeccion(
                        nombreItem = it.getString(0) ?: "",
                        resultado = it.getString(1) ?: "",
                        observacion = it.getString(2) ?: ""
                    )
                )
            }
        }
        return items
    }

    /** Actualiza la fila del ítem; si no existía (checklist cambiado), la inserta. */
    private fun actualizarItem(db: SQLiteDatabase, idInspeccion: Long, item: ItemInspeccion) {
        val valores = ContentValues()
        valores.put("resultado", item.resultado)
        valores.put("observacion", item.observacion)
        val filas = db.update(
            "item", valores,
            "idInspeccion = ? AND nombreItem = ?",
            arrayOf(idInspeccion.toString(), item.nombreItem)
        )
        if (filas == 0) {
            db.insertOrThrow("item", null, valoresDeItem(idInspeccion, item))
        }
    }

    private fun valoresDeItem(idInspeccion: Long, item: ItemInspeccion): ContentValues {
        val valores = ContentValues()
        valores.put("idInspeccion", idInspeccion)
        valores.put("nombreItem", item.nombreItem)
        valores.put("resultado", item.resultado)
        valores.put("observacion", item.observacion)
        return valores
    }

    private fun cambiarEstadoOrden(db: SQLiteDatabase, idOrden: Int, estado: String) {
        val valores = ContentValues()
        valores.put("estado", estado)
        db.update("orden", valores, "idOrden = ?", arrayOf(idOrden.toString()))
    }

    companion object {
        private const val TAG = "InspeccionDao"
    }
}
