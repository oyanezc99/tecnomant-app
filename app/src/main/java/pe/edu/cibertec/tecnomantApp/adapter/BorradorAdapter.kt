package pe.edu.cibertec.tecnomantApp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.tecnomantApp.Borrador
import pe.edu.cibertec.tecnomantApp.Fechas
import pe.edu.cibertec.tecnomantApp.R
import pe.edu.cibertec.tecnomantApp.databinding.ItemBorradorBinding

class BorradorAdapter(
    private val listaBorradores: List<Borrador>,
    private val alEditar: (Borrador) -> Unit,
    private val alBorrar: (Borrador) -> Unit,
    private val alSincronizar: (Borrador) -> Unit // <-- Cambiado de () -> Unit a (Borrador) -> Unit
) : RecyclerView.Adapter<BorradorAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBorradorBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBorradorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val borrador = listaBorradores[position]
        val contexto = holder.itemView.context
        holder.binding.apply {
            tvCodigo.text = borrador.equipoCodigo
            tvCliente.text = borrador.cliente
            tvFecha.text = "Guardada: ${Fechas.mostrar(borrador.fechaHora)}"

            if (borrador.sincronizada) {
                tvEstado.text = "Sincronizada"
                tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.estado_completada))
                btnEditar.visibility = View.GONE
                btnBorrar.visibility = View.GONE
                btnSincronizar.visibility = View.GONE // Ocultar si ya está sincronizado
            } else {
                tvEstado.text = "Pendiente (borrador)"
                tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.estado_pendiente))
                btnEditar.visibility = View.VISIBLE
                btnBorrar.visibility = View.VISIBLE
                btnSincronizar.visibility = View.VISIBLE // Mostrar botón de sincronizar
            }

            btnEditar.setOnClickListener { alEditar(borrador) }
            btnBorrar.setOnClickListener { alBorrar(borrador) }
            btnSincronizar.setOnClickListener { alSincronizar(borrador) } // <-- Vinculación del botón
        }
    }

    override fun getItemCount(): Int = listaBorradores.size
}
