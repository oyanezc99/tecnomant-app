package pe.edu.cibertec.tecnomantApp

/** Inspección guardada en la tabla local `inspeccion`, con sus ítems de la tabla `item`. */
data class Inspeccion(
    val id: Long,
    val idOrden: Int,
    val fechaHora: String,
    val observaciones: String,
    val estadoGeneral: String,
    val sincronizada: Boolean,
    val items: List<ItemInspeccion>
)
