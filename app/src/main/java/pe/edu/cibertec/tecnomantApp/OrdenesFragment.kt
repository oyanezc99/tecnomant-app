package pe.edu.cibertec.tecnomantApp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import pe.edu.cibertec.tecnomantApp.adapter.OrdenAdapter
import pe.edu.cibertec.tecnomantApp.databinding.FragmentOrdenesBinding
import pe.edu.cibertec.tecnomantApp.db.OrdenDao
import pe.edu.cibertec.tecnomantApp.retrofit.ClienteTecnoMantRetrofit
import pe.edu.cibertec.tecnomantApp.retrofit.response.OrdenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdenesFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentOrdenesBinding? = null
    private val binding get() = _binding!!

    private lateinit var ordenDao: OrdenDao
    private var request: Call<List<OrdenResponse>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ordenDao = OrdenDao(requireContext())
        binding.tvTecnico.text = Sesion.nombre(requireContext())
        binding.rvOrdenes.layoutManager = LinearLayoutManager(requireContext())
        binding.btnActualizar.setOnClickListener(this)
        binding.btnCerrarSesion.setOnClickListener(this)
        // La búsqueda filtra a medida que se escribe, consultando SQLite.
        binding.etBuscar.doAfterTextChanged {
            if (_binding != null) cargarOrdenes()
        }
    }

    override fun onResume() {
        super.onResume()
        // Se recarga al volver del detalle o del formulario para reflejar los cambios.
        cargarOrdenes()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnActualizar -> actualizarOrdenes()
            R.id.btnCerrarSesion -> cerrarSesion()
        }
    }

    /** Lee la lista de SQLite (toda, o filtrada si hay texto en Buscar). Funciona sin internet. */
    private fun cargarOrdenes() {
        val texto = binding.etBuscar.text.toString().trim()
        val resultado = if (texto.isEmpty()) ordenDao.listar() else ordenDao.buscar(texto)
        if (!resultado.ok) {
            Toast.makeText(
                requireContext(), "No se pudieron leer las órdenes guardadas", Toast.LENGTH_SHORT
            ).show()
        }
        val lista = resultado.dato ?: emptyList()
        binding.rvOrdenes.adapter = OrdenAdapter(lista) { orden -> abrirDetalle(orden) }
        binding.tvVacio.text = if (texto.isEmpty()) {
            "No hay órdenes. Pulsa Actualizar"
        } else {
            "No hay órdenes que coincidan con la búsqueda"
        }
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun actualizarOrdenes() {
        val idTecnico = Sesion.idTecnico(requireContext())
        if (idTecnico <= 0) {
            cerrarSesion()
            return
        }
        binding.btnActualizar.isEnabled = false
        binding.pbCarga.visibility = View.VISIBLE
        binding.tvEstado.text = "Cargando órdenes..."
        val llamada = ClienteTecnoMantRetrofit.retrofitTecnoMantService.obtenerOrdenes(idTecnico)
        request = llamada
        llamada.enqueue(object : Callback<List<OrdenResponse>> {
            override fun onResponse(
                call: Call<List<OrdenResponse>>, response: Response<List<OrdenResponse>>
            ) {
                if (_binding == null || call.isCanceled) return
                finalizarCarga()
                val recibidas = response.body()
                if (response.isSuccessful && recibidas != null) {
                    guardarOrdenes(recibidas)
                } else {
                    mostrarError("No se pudieron actualizar las órdenes (HTTP ${response.code()}).")
                }
            }

            override fun onFailure(call: Call<List<OrdenResponse>>, t: Throwable) {
                if (_binding == null || call.isCanceled) return
                finalizarCarga()
                mostrarError("Sin conexión: mostrando datos guardados")
            }
        })
    }

    /** Valida lo que llegó de la API y lo guarda en SQLite. */
    private fun guardarOrdenes(recibidas: List<OrdenResponse>) {
        val ordenes = ArrayList<Orden>()
        for (recibida in recibidas) {
            val orden = convertirOrden(recibida)
            if (orden == null) {
                mostrarError("La respuesta del servidor trae una orden sin datos. No se guardó nada.")
                return
            }
            ordenes.add(orden)
        }
        if (ordenDao.guardarOrdenesDelServidor(ordenes)) {
            binding.tvEstado.text = "Órdenes actualizadas: ${ordenes.size}"
            cargarOrdenes()
        } else {
            mostrarError("No se pudieron guardar las órdenes en el celular")
        }
    }

    /** Devuelve null si a la orden le falta el id o el estado. */
    private fun convertirOrden(recibida: OrdenResponse): Orden? {
        val idOrden = recibida.idOrden ?: return null
        val estado = recibida.estado
        if (idOrden <= 0 || estado.isNullOrBlank()) return null
        return Orden(
            idOrden = idOrden,
            cliente = recibida.cliente ?: "",
            direccion = recibida.direccion ?: "",
            equipoTipo = recibida.equipoTipo ?: "",
            equipoCodigo = recibida.equipoCodigo ?: "",
            ubicacion = recibida.ubicacion ?: "",
            fechaProgramada = recibida.fechaProgramada ?: "",
            estado = estado
        )
    }

    private fun finalizarCarga() {
        binding.btnActualizar.isEnabled = true
        binding.pbCarga.visibility = View.GONE
    }

    private fun mostrarError(mensaje: String) {
        binding.tvEstado.text = mensaje
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun abrirDetalle(orden: Orden) {
        val intent = Intent(requireContext(), DetalleOrdenActivity::class.java)
        intent.putExtra(DetalleOrdenActivity.EXTRA_ID_ORDEN, orden.idOrden)
        startActivity(intent)
    }

    /** Borra solo la sesión y vuelve al login. Las órdenes y borradores de SQLite se conservan. */
    private fun cerrarSesion() {
        Sesion.borrar(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        request?.cancel()
        request = null
        binding.rvOrdenes.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
