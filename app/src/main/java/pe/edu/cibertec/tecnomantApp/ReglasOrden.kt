package pe.edu.cibertec.tecnomantApp

/**
 * Reglas de las órdenes de trabajo.
 * Lógica pura (sin Android) para poder probarla con JUnit.
 */
object ReglasOrden {

    const val PENDIENTE = "Pendiente"
    const val EN_PROCESO = "En proceso"
    const val COMPLETADA = "Completada"

    /**
     * Decide qué órdenes locales se borran al pulsar "Actualizar órdenes":
     * las que ya no vienen del servidor y no tienen inspección guardada.
     * Una orden con inspección guardada nunca se borra.
     */
    fun idsABorrar(
        idsLocales: List<Int>,
        idsServidor: List<Int>,
        idsConInspeccion: List<Int>
    ): List<Int> {
        return idsLocales.filter { !idsServidor.contains(it) && !idsConInspeccion.contains(it) }
    }
}
