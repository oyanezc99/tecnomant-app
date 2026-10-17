package pe.edu.cibertec.tecnomantApp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pe.edu.cibertec.tecnomantApp.databinding.ActivityLoginBinding
import pe.edu.cibertec.tecnomantApp.retrofit.ClienteTecnoMantRetrofit
import pe.edu.cibertec.tecnomantApp.retrofit.request.LoginRequest
import pe.edu.cibertec.tecnomantApp.retrofit.response.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private var request: Call<LoginResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Con una sesión guardada se va directo al Home, sin mostrar el login.
        if (Sesion.haySesion(this)) {
            irAHome()
            return
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val margen = binding.root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(
                margen + systemBars.left, margen + systemBars.top,
                margen + systemBars.right, margen + systemBars.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
        binding.btnIngresar.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        if (v?.id == R.id.btnIngresar) {
            validarLogin()
        }
    }

    private fun validarLogin() {
        val usuario = binding.etUsuario.text.toString().trim()
        val clave = binding.etClave.text.toString()
        if (usuario.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese usuario y clave", Toast.LENGTH_SHORT).show()
            return
        }
        iniciarSesion(usuario, clave)
    }
    private fun iniciarSesion(usuario: String, clave: String) {
        binding.btnIngresar.isEnabled = false
        binding.pbCarga.visibility = View.VISIBLE
        val llamada = ClienteTecnoMantRetrofit.retrofitTecnoMantService
            .iniciarSesion(LoginRequest(usuario, clave))
        request = llamada
        llamada.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (isFinishing || isDestroyed || call.isCanceled) return
                finalizarCarga()
                procesarRespuesta(response)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                if (isFinishing || isDestroyed || call.isCanceled) return
                finalizarCarga()
                mostrarMensaje("Sin conexión. Inicia sesión con internet la primera vez")
            }
        })
    }
    private fun procesarRespuesta(response: Response<LoginResponse>) {
        if (response.code() == 401) {
            mostrarMensaje("Usuario o clave incorrectos")
            return
        }
        val datos = response.body()
        if (!response.isSuccessful || datos == null) {
            mostrarMensaje("No se pudo iniciar sesión (HTTP ${response.code()}).")
            return
        }
        if (datos.rol != ROL_TECNICO) {
            mostrarMensaje("Esta app es solo para técnicos")
            return
        }
        val idTecnico = datos.idTecnico
        if (idTecnico == null || idTecnico <= 0) {
            mostrarMensaje("No se pudo iniciar sesión: la respuesta no trae el técnico.")
            return
        }
        Sesion.guardar(this, idTecnico, datos.nombre ?: "")
        irAHome()
    }
    private fun finalizarCarga() {
        binding.btnIngresar.isEnabled = true
        binding.pbCarga.visibility = View.GONE
    }
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun irAHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    override fun onDestroy() {
        request?.cancel()
        request = null
        super.onDestroy()
    }
    companion object {
        private const val ROL_TECNICO = "tecnico"
    }
}
