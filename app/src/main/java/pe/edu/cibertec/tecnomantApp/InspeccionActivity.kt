package pe.edu.cibertec.tecnomantApp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import pe.edu.cibertec.tecnomantApp.adapter.ChecklistAdapter
import pe.edu.cibertec.tecnomantApp.databinding.ActivityInspeccionBinding
import pe.edu.cibertec.tecnomantApp.db.InspeccionDao
import pe.edu.cibertec.tecnomantApp.db.OrdenDao
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Formulario de inspección. Sirve para registrar una inspección nueva y para
 * editar un borrador: si la orden ya tiene inspección, se cargan sus datos.
 */
class InspeccionActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityInspeccionBinding
    private lateinit var inspeccionDao: InspeccionDao

    private var idOrden = -1
    private var idInspeccion: Long? = null
    private var items: List<ItemInspeccion> = emptyList()

    // Por aqui guardamos de manera temporal las rutas de las fotos de esta inspeccion (solo en memoria)
    private val rutasFotos = ArrayList<String>()

    private val rutasEnBd = ArrayList<String>()
    private var inspeccionGuardada = false

    private val lanzadorEvidencia = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val rutas = resultado.data?.getStringArrayListExtra(EvidenciaActivity.EXTRA_RUTAS)
            if (rutas != null) {
                // En esta parte limpiamos las fotos eliminadas que no alcanzaron a guardarse en el SQLIte
                for (anterior in rutasFotos) {
                    if (!rutas.contains(anterior) && !rutasEnBd.contains(anterior)) {
                        ArchivosEvidencia.borrar(anterior)
                    }
                }
                rutasFotos.clear()
                rutasFotos.addAll(rutas)
                mostrarContadorFotos()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspeccionBinding.inflate(layoutInflater)
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

        inspeccionDao = InspeccionDao(this)
        idOrden = intent.getIntExtra(EXTRA_ID_ORDEN, -1)
        if (!cargarFormulario(savedInstanceState)) {
            finish()
            return
        }
        if (savedInstanceState != null) {
            rutasFotos.addAll(savedInstanceState.getStringArrayList(ESTADO_FOTOS) ?: arrayListOf())
        }
        mostrarContadorFotos()
        binding.btnGuardar.setOnClickListener(this)
        binding.btnAgregarFotos.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnGuardar -> guardarInspeccion()
            R.id.btnAgregarFotos -> irAEvidencia()
        }
    }

    private fun irAEvidencia() {
        val intent = Intent(this, EvidenciaActivity::class.java)
        intent.putStringArrayListExtra(EvidenciaActivity.EXTRA_RUTAS, ArrayList(rutasFotos))
        lanzadorEvidencia.launch(intent)
    }

    private fun mostrarContadorFotos() {
        binding.tvFotos.text = "Fotos: ${rutasFotos.size}"
    }

    /** Arma el formulario. Devuelve false (con su Toast) si no se puede inspeccionar la orden. */
    private fun cargarFormulario(savedInstanceState: Bundle?): Boolean {
        val resultadoOrden = OrdenDao(this).obtener(idOrden)
        val orden = resultadoOrden.dato
        if (!resultadoOrden.ok || orden == null) {
            mostrarMensaje(if (resultadoOrden.ok) "No se encontró la orden" else "No se pudo leer la orden guardada")
            return false
        }
        val nombres = Checklist.itemsPara(orden.equipoTipo)
        if (nombres.isEmpty()) {
            mostrarMensaje("Tipo de equipo no reconocido: no se puede inspeccionar")
            return false
        }
        val resultadoInspeccion = inspeccionDao.obtenerPorOrden(idOrden)
        if (!resultadoInspeccion.ok) {
            mostrarMensaje("No se pudo leer la inspección guardada")
            return false
        }
        val guardada = resultadoInspeccion.dato
        if (guardada != null && guardada.sincronizada) {
            // RN-02: una inspección sincronizada no se abre para edición.
            mostrarMensaje("Ya sincronizada (solo lectura)")
            return false
        }

        idInspeccion = guardada?.id

        if (guardada != null) {
            val resultadoFotos = inspeccionDao.listarEvidencias(guardada.id)
            if (!resultadoFotos.ok) {
                mostrarMensaje("No se pudieron leer las fotos guardadas")
                return false
            }
            rutasEnBd.addAll(resultadoFotos.dato ?: emptyList())
            if (savedInstanceState == null) rutasFotos.addAll(rutasEnBd)
        }

        items = nombres.map { nombre ->
            val itemGuardado = guardada?.items?.firstOrNull { it.nombreItem == nombre }
            ItemInspeccion(nombre, itemGuardado?.resultado ?: "", itemGuardado?.observacion ?: "")
        }
        if (savedInstanceState == null) {
            binding.etObservaciones.setText(guardada?.observaciones ?: "")
        } else {
            restaurarItems(savedInstanceState)
        }

        binding.tvTitulo.text = "Inspección: ${orden.equipoCodigo}"
        binding.tvEquipo.text = "${orden.equipoTipo} · ${orden.cliente}"
        binding.rvChecklist.layoutManager = LinearLayoutManager(this)
        binding.rvChecklist.adapter = ChecklistAdapter(items)
        return true
    }

    private fun guardarInspeccion() {
        if (!ReglasInspeccion.todosTienenResultado(items)) {
            mostrarMensaje("Marca Bien, Regular o Malo en todos los ítems")
            return
        }
        // RN-01 (avance): cada ítem en Malo necesita su observación.
        val malosSinObservacion = ReglasInspeccion.malosSinObservacion(items)
        if (malosSinObservacion.isNotEmpty()) {
            mostrarMensaje("Escribe la observación del ítem en Malo: ${malosSinObservacion[0].nombreItem}")
            return
        }
        // RN- 01: Aqui se cumple que si hay un ítem en Malo, también te exigira al menos 1 foto de prueba
        if (ReglasInspeccion.faltaFoto(items, rutasFotos.size)) {
            mostrarMensaje("Agrega al menos una foto: hay ítems en Malo")
            return
        }

        // Solo los ítems en Malo llevan observación.
        val itemsAGuardar = items.map { item ->
            val observacion = if (item.resultado == ReglasInspeccion.MALO) item.observacion.trim() else ""
            ItemInspeccion(item.nombreItem, item.resultado, observacion)
        }
        val observaciones = binding.etObservaciones.text.toString().trim()
        val estadoGeneral = ReglasInspeccion.calcularEstadoGeneral(itemsAGuardar)
        val fechaHora = Fechas.ahora()

        val id = idInspeccion
        val guardado = if (id == null) {
            inspeccionDao.insertar(idOrden, fechaHora, observaciones, estadoGeneral, itemsAGuardar, rutasFotos)
        } else {
            inspeccionDao.actualizar(id, idOrden, fechaHora, observaciones, estadoGeneral, itemsAGuardar, rutasFotos)
        }
        if (!guardado) {
            mostrarMensaje("No se pudo guardar la inspección")
            return
        }
        // En esta parte se marca para que onDestroy no borre las fotos que ya quedaron en SQLite
        inspeccionGuardada = true
        mostrarMensaje("Inspección guardada (estado general: $estadoGeneral)")
        volverALaLista()
    }

    /** Vuelve a la pantalla principal; la lista se recarga sola al mostrarse. */
    private fun volverALaLista() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    /** Al girar la pantalla se conserva lo que el técnico ya marcó y todavía no guardó. */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(ESTADO_RESULTADOS, ArrayList(items.map { it.resultado }))
        outState.putStringArrayList(ESTADO_OBSERVACIONES, ArrayList(items.map { it.observacion }))
        outState.putStringArrayList(ESTADO_FOTOS, ArrayList(rutasFotos))
    }

    override fun onDestroy() {
        if (isFinishing && !inspeccionGuardada) {
            for (ruta in rutasFotos) {
                if (!rutasEnBd.contains(ruta)) ArchivosEvidencia.borrar(ruta)
            }
        }
        super.onDestroy()
    }

    private fun restaurarItems(savedInstanceState: Bundle) {
        val resultados = savedInstanceState.getStringArrayList(ESTADO_RESULTADOS) ?: return
        val observaciones = savedInstanceState.getStringArrayList(ESTADO_OBSERVACIONES) ?: return
        if (resultados.size != items.size || observaciones.size != items.size) return
        for (posicion in items.indices) {
            items[posicion].resultado = resultados[posicion]
            items[posicion].observacion = observaciones[posicion]
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_ID_ORDEN = "idOrden"
        private const val ESTADO_RESULTADOS = "resultados"
        private const val ESTADO_OBSERVACIONES = "observaciones"
        private const val ESTADO_FOTOS = "fotos"
    }
}
