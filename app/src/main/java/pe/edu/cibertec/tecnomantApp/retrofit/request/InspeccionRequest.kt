package pe.edu.cibertec.tecnomantApp.retrofit.request
data class InspeccionRequest(
    val idOrden: Int,
    val fechaHora: String,
    val observaciones: String,
    val estadoGeneral: String,
    val items: List<ItemInspeccionRequest>
)



