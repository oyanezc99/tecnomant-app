package pe.edu.cibertec.tecnomantApp.adapter

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import pe.edu.cibertec.tecnomantApp.ItemInspeccion
import pe.edu.cibertec.tecnomantApp.R
import pe.edu.cibertec.tecnomantApp.ReglasInspeccion
import pe.edu.cibertec.tecnomantApp.databinding.ItemChecklistBinding

/**
 * Checklist del formulario de inspección.
 * Lo que el técnico marca o escribe se guarda directamente en la lista de ítems.
 */
class ChecklistAdapter(private val listaItems: List<ItemInspeccion>) :
    RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChecklistBinding) : RecyclerView.ViewHolder(binding.root) {
        var observador: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChecklistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaItems[position]
        holder.binding.apply {
            // Se quitan los avisos anteriores para que mostrar los datos no cambie otro ítem.
            rgResultado.setOnCheckedChangeListener(null)
            etObservacion.removeTextChangedListener(holder.observador)

            tvNombreItem.text = item.nombreItem
            when (item.resultado) {
                ReglasInspeccion.BIEN -> rgResultado.check(R.id.rbBien)
                ReglasInspeccion.REGULAR -> rgResultado.check(R.id.rbRegular)
                ReglasInspeccion.MALO -> rgResultado.check(R.id.rbMalo)
                else -> rgResultado.clearCheck()
            }
            etObservacion.setText(item.observacion)
            tilObservacion.visibility = visibilidadObservacion(item)

            rgResultado.setOnCheckedChangeListener { _, idMarcado ->
                item.resultado = when (idMarcado) {
                    R.id.rbBien -> ReglasInspeccion.BIEN
                    R.id.rbRegular -> ReglasInspeccion.REGULAR
                    R.id.rbMalo -> ReglasInspeccion.MALO
                    else -> ""
                }
                tilObservacion.visibility = visibilidadObservacion(item)
            }
            holder.observador = etObservacion.doAfterTextChanged { texto ->
                item.observacion = texto?.toString() ?: ""
            }
        }
    }

    override fun getItemCount(): Int = listaItems.size

    /** La observación del ítem solo se ve cuando el ítem está en Malo. */
    private fun visibilidadObservacion(item: ItemInspeccion): Int {
        return if (item.resultado == ReglasInspeccion.MALO) View.VISIBLE else View.GONE
    }
}
