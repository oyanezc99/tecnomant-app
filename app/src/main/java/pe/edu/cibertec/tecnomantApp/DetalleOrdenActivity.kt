package pe.edu.cibertec.tecnomantApp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pe.edu.cibertec.tecnomantApp.adapter.OrdenAdapter
import pe.edu.cibertec.tecnomantApp.databinding.ActivityDetalleOrdenBinding
import pe.edu.cibertec.tecnomantApp.db.InspeccionDao
import pe.edu.cibertec.tecnomantApp.db.OrdenDao

class DetalleOrdenActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityDetalleOrdenBinding
    private var idOrden = -1
    private var orden: Orden? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val margen = binding.root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                margen + systemBars.left, margen + systemBars.top,
                margen + systemBars.right, margen + systemBars.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        idOrden = intent.getIntExtra(EXTRA_ID_ORDEN, -1)
        binding.btnPrincipal.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        cargarDetalle()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnPrincipal) {
            abrirInspeccion()
        }
    }

    /** Lee la orden de SQLite y muestra sus datos. */
    private fun cargarDetalle() {
        val resultado = OrdenDao(this).obtener(idOrden)
        val ordenLeida = resultado.dato
        if (!resultado.ok || ordenLeida == null) {
            val mensaje = if (resultado.ok) "No se encontró la orden" else "No se pudo leer la orden guardada"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        orden = ordenLeida
        binding.tvCliente.text = "Cliente: ${ordenLeida.cliente}"
        binding.tvDireccion.text = "Dirección: ${ordenLeida.direccion}"
        binding.tvEquipo.text = "Equipo: ${ordenLeida.equipoTipo} · ${ordenLeida.equipoCodigo}"
        binding.tvUbicacion.text = "Ubicación: ${ordenLeida.ubicacion}"
        binding.tvFecha.text = "Fecha programada: ${Fechas.mostrar(ordenLeida.fechaProgramada)}"
        binding.tvEstado.text = ordenLeida.estado
        binding.tvEstado.setBackgroundColor(OrdenAdapter.colorDeEstado(this, ordenLeida.estado))
        configurarBoton()
    }
    /** El botón principal depende de si la orden tiene inspección y de si ya se sincronizó. */
    private fun configurarBoton() {
        val resultado = InspeccionDao(this).obtenerPorOrden(idOrden)
        val inspeccion = resultado.dato
        if (!resultado.ok) {
            Toast.makeText(this, "No se pudo leer la inspección guardada", Toast.LENGTH_SHORT).show()
            binding.btnPrincipal.text = "Iniciar inspección"
            binding.btnPrincipal.isEnabled = false
        } else if (inspeccion == null) {
            binding.btnPrincipal.text = "Iniciar inspección"
            binding.btnPrincipal.isEnabled = true
        } else if (!inspeccion.sincronizada) {
            binding.btnPrincipal.text = "Editar borrador"
            binding.btnPrincipal.isEnabled = true
        } else {
            // RN-02: una inspección sincronizada no se modifica.
            binding.btnPrincipal.text = "Ya sincronizada (solo lectura)"
            binding.btnPrincipal.isEnabled = false
        }
    }
    private fun abrirInspeccion() {
        val ordenActual = orden ?: return
        if (Checklist.itemsPara(ordenActual.equipoTipo).isEmpty()) {
            Toast.makeText(
                this, "Tipo de equipo no reconocido: no se puede inspeccionar", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(this, InspeccionActivity::class.java)
        intent.putExtra(InspeccionActivity.EXTRA_ID_ORDEN, ordenActual.idOrden)
        startActivity(intent)
    }
    companion object {
        const val EXTRA_ID_ORDEN = "idOrden"
    }
}
