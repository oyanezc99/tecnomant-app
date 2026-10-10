package pe.edu.cibertec.tecnomantApp

/**
 * Ítem del checklist de una inspección.
 * `resultado` vacío significa que el técnico todavía no marcó Bien, Regular ni Malo.
 */
data class ItemInspeccion(
    val nombreItem: String,
    var resultado: String = "",
    var observacion: String = ""
)
