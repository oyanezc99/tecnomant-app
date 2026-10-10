package pe.edu.cibertec.tecnomantApp

/** Fila de la pantalla Borradores: una inspección guardada con los datos de su orden. */
data class Borrador(
    val idInspeccion: Long,
    val idOrden: Int,
    val equipoCodigo: String,
    val cliente: String,
    val fechaHora: String,
    val sincronizada: Boolean
)
