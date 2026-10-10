package pe.edu.cibertec.tecnomantApp.retrofit.response

/**
 * Elemento de la respuesta de GET api/ordenes/tecnico/{idTecnico}.
 * Los campos son opcionales porque llegan de fuera; la pantalla los valida antes de guardarlos.
 */
data class OrdenResponse(
    val idOrden: Int?,
    val cliente: String?,
    val direccion: String?,
    val equipoTipo: String?,
    val equipoCodigo: String?,
    val ubicacion: String?,
    val fechaProgramada: String?,
    val estado: String?
)
