package pe.edu.cibertec.tecnomantApp

/**
 * Reglas del formulario de inspección.
 * Lógica pura (sin Android) para poder probarla con JUnit.
 */
object ReglasInspeccion {

    const val BIEN = "Bien"
    const val REGULAR = "Regular"
    const val MALO = "Malo"

    /** Todos los ítems deben tener un resultado (Bien, Regular o Malo). Un checklist vacío no pasa. */
    fun todosTienenResultado(items: List<ItemInspeccion>): Boolean {
        if (items.isEmpty()) return false
        return items.all { it.resultado == BIEN || it.resultado == REGULAR || it.resultado == MALO }
    }

    /**
     * RN-01 (avance): cada ítem en Malo debe tener observación.
     * Devuelve los ítems en Malo cuya observación está vacía o solo tiene espacios.
     */
    fun malosSinObservacion(items: List<ItemInspeccion>): List<ItemInspeccion> {
        return items.filter { it.resultado == MALO && it.observacion.isBlank() }
    }

    /** RN-08: Malo si algún ítem es Malo; si no, Regular si alguno es Regular; si no, Bien. */
    fun calcularEstadoGeneral(items: List<ItemInspeccion>): String {
        if (items.any { it.resultado == MALO }) return MALO
        if (items.any { it.resultado == REGULAR }) return REGULAR
        return BIEN
    }
}
