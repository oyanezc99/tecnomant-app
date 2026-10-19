package pe.edu.cibertec.tecnomantApp.retrofit.response

/**
 * Respuesta 200 de POST api/login.
 * Los campos son opcionales porque llegan de fuera: `idTecnico` es null para el administrador.
 */
data class LoginResponse(
    val token: String? = null,
    val rol: String?,
    val idTecnico: Int?,
    val nombre: String?
)
