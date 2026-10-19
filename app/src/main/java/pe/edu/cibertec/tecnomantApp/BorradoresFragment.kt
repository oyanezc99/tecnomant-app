package pe.edu.cibertec.tecnomantApp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import pe.edu.cibertec.tecnomantApp.adapter.BorradorAdapter
import pe.edu.cibertec.tecnomantApp.databinding.FragmentBorradoresBinding
import pe.edu.cibertec.tecnomantApp.db.InspeccionDao
import pe.edu.cibertec.tecnomantApp.retrofit.ClienteTecnoMantRetrofit
import pe.edu.cibertec.tecnomantApp.retrofit.request.InspeccionRequest
import pe.edu.cibertec.tecnomantApp.retrofit.request.ItemInspeccionRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BorradoresFragment : Fragment() {
    private var _binding: FragmentBorradoresBinding? = null
    private val binding get() = _binding!!
    private lateinit var inspeccionDao: InspeccionDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorradoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inspeccionDao = InspeccionDao(requireContext())
        binding.rvBorradores.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onResume() {
        super.onResume()
        cargarBorradores()
    }

    private fun cargarBorradores() {
        val resultado = inspeccionDao.listarBorradores()
        if (!resultado.ok) {
            mostrarMensaje("No se pudieron leer los borradores guardados")
        }
        val lista = resultado.dato ?: emptyList()

        binding.rvBorradores.adapter = BorradorAdapter(
            lista,
            { borrador -> editarBorrador(borrador) },
            { borrador -> confirmarBorrado(borrador) },
            { borrador -> sincronizarBorrador(borrador) } // Acción de sincronizar
        )
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun editarBorrador(borrador: Borrador) {
        val intent = Intent(requireContext(), InspeccionActivity::class.java)
        intent.putExtra(InspeccionActivity.EXTRA_ID_ORDEN, borrador.idOrden)
        startActivity(intent)
    }

    private fun confirmarBorrado(borrador: Borrador) {
        AlertDialog.Builder(requireContext())
            .setMessage("¿Borrar este borrador?")
            .setPositiveButton("Borrar") { _, _ -> borrarBorrador(borrador) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun borrarBorrador(borrador: Borrador) {
        if (_binding == null) return
        if (inspeccionDao.borrar(borrador.idInspeccion, borrador.idOrden)) {
            mostrarMensaje("Borrador borrado")
        } else {
            mostrarMensaje("No se pudo borrar el borrador")
        }
        cargarBorradores()
    }

    private fun sincronizarBorrador(borrador: Borrador) {
        mostrarMensaje("Sincronizando pendientes del más antiguo al más reciente...")
        Sincronizador.ejecutar(requireContext()) { ok,mensaje ->
            activity?.runOnUiThread {
                if(_binding!=null) { mostrarMensaje(mensaje); cargarBorradores() }
            }
            if(!ok) Sincronizador.programar(TecnoMantApplication.instancia)
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.rvBorradores.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
