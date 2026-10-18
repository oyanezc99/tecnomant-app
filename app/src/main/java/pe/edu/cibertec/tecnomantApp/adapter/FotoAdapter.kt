package pe.edu.cibertec.tecnomantApp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pe.edu.cibertec.tecnomantApp.databinding.ItemFotoBinding
import pe.edu.cibertec.tecnomantApp.databinding.ItemGaleriaBinding
import java.io.File

/**
 * Grid de fotos (de la pantalla evidencia)
 * La posición 0 es el cuadro "Galería"; las fotos empiezan en la posición 1.
 */
class FotoAdapter(
    private val listaRutas: List<String>,
    private val alElegirGaleria: () -> Unit,
    private val alQuitar: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class GaleriaViewHolder(val binding: ItemGaleriaBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class FotoViewHolder(val binding: ItemFotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TIPO_GALERIA else TIPO_FOTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_GALERIA) {
            GaleriaViewHolder(ItemGaleriaBinding.inflate(inflater, parent, false))
        } else {
            FotoViewHolder(ItemFotoBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GaleriaViewHolder) {
            holder.binding.cuadroGaleria.setOnClickListener { alElegirGaleria() }
        } else if (holder is FotoViewHolder) {
            val ruta = listaRutas[position - 1]
            Glide.with(holder.itemView.context)
                .load(File(ruta))
                .centerCrop()
                .into(holder.binding.ivMiniatura)
            holder.binding.btnQuitar.setOnClickListener { alQuitar(ruta) }
        }
    }

    override fun getItemCount(): Int = listaRutas.size + 1

    companion object {
        private const val TIPO_GALERIA = 0
        private const val TIPO_FOTO = 1
    }
}