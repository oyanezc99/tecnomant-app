package pe.edu.cibertec.tecnomantApp

/**
 * Ítems del checklist según el tipo de equipo.
 * Lógica pura (sin Android) para poder probarla con JUnit.
 */
object Checklist {

    const val ASCENSOR = "Ascensor"
    const val AIRE_ACONDICIONADO = "Aire acondicionado"
    const val ACTIVO_INDUSTRIAL = "Activo industrial"

    /** Devuelve los cinco ítems del tipo de equipo, o una lista vacía si el tipo no se conoce. */
    fun itemsPara(equipoTipo: String): List<String> {
        return when (equipoTipo.trim()) {
            ASCENSOR -> listOf(
                "Funcionamiento", "Ruidos", "Puertas", "Desgaste de cables", "Iluminación"
            )
            AIRE_ACONDICIONADO -> listOf(
                "Funcionamiento", "Ruidos", "Fugas", "Filtros", "Desgaste"
            )
            ACTIVO_INDUSTRIAL -> listOf(
                "Funcionamiento", "Ruidos", "Fugas", "Desgaste", "Protecciones"
            )
            else -> emptyList()
        }
    }
}
