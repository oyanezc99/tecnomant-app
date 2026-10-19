package pe.edu.cibertec.tecnomantApp
/** FIFO estable: solo pendientes, de la fecha más antigua a la más reciente. */
object ColaSincronizacion {
    fun pendientes(borradores: List<Borrador>): List<Borrador> = borradores.filter { !it.sincronizada }.sortedWith(compareBy<Borrador> { it.fechaHora }.thenBy { it.idInspeccion })
}
