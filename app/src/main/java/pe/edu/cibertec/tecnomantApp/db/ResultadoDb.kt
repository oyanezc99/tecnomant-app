package pe.edu.cibertec.tecnomantApp.db

/**
 * Resultado de una lectura de SQLite.
 * `ok` es false cuando la base de datos falló; en ese caso la pantalla avisa con un Toast.
 * `dato` puede ser null aunque `ok` sea true (por ejemplo, una orden que no tiene inspección).
 */
class ResultadoDb<T>(val ok: Boolean, val dato: T?)
