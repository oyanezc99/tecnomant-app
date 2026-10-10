package pe.edu.cibertec.tecnomantApp.retrofit.api

import pe.edu.cibertec.tecnomantApp.retrofit.request.LoginRequest
import pe.edu.cibertec.tecnomantApp.retrofit.response.LoginResponse
import pe.edu.cibertec.tecnomantApp.retrofit.response.OrdenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ITecnoMantService {

    @POST("api/login")
    fun iniciarSesion(@Body pedido: LoginRequest): Call<LoginResponse>

    @GET("api/ordenes/tecnico/{idTecnico}")
    fun obtenerOrdenes(@Path("idTecnico") idTecnico: Int): Call<List<OrdenResponse>>
}
