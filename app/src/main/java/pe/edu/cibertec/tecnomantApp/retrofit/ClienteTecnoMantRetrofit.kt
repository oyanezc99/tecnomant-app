package pe.edu.cibertec.tecnomantApp.retrofit

import okhttp3.OkHttpClient
import pe.edu.cibertec.tecnomantApp.retrofit.api.ITecnoMantService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteTecnoMantRetrofit {

    /**
     * URL base de la API de TecnoMant. Es el único lugar donde aparece.
     * 10.0.2.2 es la computadora vista desde el emulador de Android.
     * Para un celular real se cambia por la IP de la computadora en la red local
     * (por ejemplo "http://192.168.1.50:5080/"), con la API escuchando en 0.0.0.0:5080.
     */
    private const val URL_BASE = "http://10.0.2.2:5080/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { cadena ->
            val token=pe.edu.cibertec.tecnomantApp.Sesion.token(pe.edu.cibertec.tecnomantApp.TecnoMantApplication.instancia)
            val pedido=cadena.request().newBuilder()
            if(token.isNotBlank()) pedido.header("Authorization", "Bearer "+token)
            cadena.proceed(pedido.build())
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit() = Retrofit.Builder()
        .baseUrl(URL_BASE)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitTecnoMantService: ITecnoMantService by lazy {
        buildRetrofit().create(ITecnoMantService::class.java)
    }
}
