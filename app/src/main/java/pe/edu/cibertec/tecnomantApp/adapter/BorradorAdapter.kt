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
    private val alBorrar: (Borrador) -> Unit
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
                // RN-02: los sincronizados son de solo lectura, sin Editar ni Borrar.
                tvEstado.text = "Sincronizada"
                tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.estado_completada))
                btnEditar.visibility = View.GONE
                btnBorrar.visibility = View.GONE
            } else {
                tvEstado.text = "Pendiente (borrador)"
                tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.estado_pendiente))
                btnEditar.visibility = View.VISIBLE
                btnBorrar.visibility = View.VISIBLE
            }
            btnEditar.setOnClickListener { alEditar(borrador) }
            btnBorrar.setOnClickListener { alBorrar(borrador) }
        }
    }

    override fun getItemCount(): Int = listaBorradores.size
}
