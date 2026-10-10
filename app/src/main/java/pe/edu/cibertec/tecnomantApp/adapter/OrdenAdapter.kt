package pe.edu.cibertec.tecnomantApp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.tecnomantApp.Fechas
import pe.edu.cibertec.tecnomantApp.Orden
import pe.edu.cibertec.tecnomantApp.R
import pe.edu.cibertec.tecnomantApp.ReglasOrden
import pe.edu.cibertec.tecnomantApp.databinding.ItemOrdenBinding

class OrdenAdapter(
    private val listaOrdenes: List<Orden>,
    private val alTocarOrden: (Orden) -> Unit
) : RecyclerView.Adapter<OrdenAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrdenBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrdenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orden = listaOrdenes[position]
        val color = colorDeEstado(holder.itemView.context, orden.estado)
        holder.binding.apply {
            tvCliente.text = orden.cliente
            tvEquipo.text = "${orden.equipoTipo} · ${orden.equipoCodigo}"
            tvFecha.text = "Programada: ${Fechas.mostrar(orden.fechaProgramada)}"
            tvEstado.text = orden.estado
            tvEstado.setBackgroundColor(color)
            viewBarra.setBackgroundColor(color)
            root.setOnClickListener { alTocarOrden(orden) }
        }
    }

    override fun getItemCount(): Int = listaOrdenes.size

    companion object {
        /** Color de la etiqueta y de la barra según el estado de la orden. */
        fun colorDeEstado(context: Context, estado: String): Int {
            val idColor = when (estado) {
                ReglasOrden.PENDIENTE -> R.color.estado_pendiente
                ReglasOrden.EN_PROCESO -> R.color.estado_en_proceso
                ReglasOrden.COMPLETADA -> R.color.estado_completada
                else -> R.color.texto_secundario
            }
            return ContextCompat.getColor(context, idColor)
        }
    }
}
