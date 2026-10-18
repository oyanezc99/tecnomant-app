package pe.edu.cibertec.tecnomantApp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import pe.edu.cibertec.tecnomantApp.adapter.FotoAdapter
import pe.edu.cibertec.tecnomantApp.databinding.ActivityEvidenciaBinding
import java.io.File

/**
 * Aqui se gestiona la pantalla de evidencia fotografica, recibe las rutas acuales de la inspeccion y retorna
 * la lista final al formulario para que las guarde en el SQLite
 */
class EvidenciaActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEvidenciaBinding

    private val rutas = ArrayList<String>()
    private val nuevas = ArrayList<String>()
    private var archivoPendiente: File? = null

    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val archivo = archivoPendiente
        archivoPendiente = null
        if (archivo != null) {
            if (tomada && ArchivosEvidencia.reducir(archivo)) {
                agregarFoto(archivo)
            } else {
                archivo.delete()
                if (tomada) mostrarMensaje("No se pudo procesar la foto")
            }
        }
    }

    private val elegirDeGaleria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val archivo = ArchivosEvidencia.copiarDesdeUri(this, uri)
            if (archivo != null) {
                agregarFoto(archivo)
            } else {
                mostrarMensaje("No se pudo leer la foto elegida")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvidenciaBinding.inflate(layoutInflater)
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

        if (savedInstanceState == null) {
            rutas.addAll(intent.getStringArrayListExtra(EXTRA_RUTAS) ?: arrayListOf())
        } else {
            rutas.addAll(savedInstanceState.getStringArrayList(ESTADO_RUTAS) ?: arrayListOf())
            nuevas.addAll(savedInstanceState.getStringArrayList(ESTADO_NUEVAS) ?: arrayListOf())
            val pendiente = savedInstanceState.getString(ESTADO_PENDIENTE)
            if (pendiente != null) archivoPendiente = File(pendiente)
        }

        binding.rvFotos.layoutManager = GridLayoutManager(this, 3)
        binding.btnTomarFoto.setOnClickListener(this)
        binding.btnGuardarFotos.setOnClickListener(this)
        // El botón Atrás también devuelve las fotos, para no perder lo ya tomado.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                devolverFotos()
            }
        })
        mostrarFotos()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnTomarFoto -> abrirCamara()
            R.id.btnGuardarFotos -> devolverFotos()
        }
    }

    private fun abrirCamara() {
        val archivo = ArchivosEvidencia.crearArchivo(this)
        archivoPendiente = archivo
        try {
            tomarFoto.launch(ArchivosEvidencia.uriDe(this, archivo))
        } catch (e: ActivityNotFoundException) {
            archivoPendiente = null
            archivo.delete()
            mostrarMensaje("No hay una app de cámara en este celular")
        }
    }

    private fun agregarFoto(archivo: File) {
        rutas.add(archivo.absolutePath)
        nuevas.add(archivo.absolutePath)
        mostrarFotos()
    }

    private fun confirmarQuitar(ruta: String) {
        AlertDialog.Builder(this)
            .setMessage("¿Quitar esta foto?")
            .setPositiveButton("Quitar") { _, _ -> quitarFoto(ruta) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun quitarFoto(ruta: String) {
        rutas.remove(ruta)
        // aqui solo se borra el archivo si se creó en esta pantalla; los demás ya se encarga el formulario de limpiarlo.
        if (nuevas.remove(ruta)) ArchivosEvidencia.borrar(ruta)
        mostrarFotos()
    }

    /** En esta parte se dibuja la grilla, se actualiza el contador de fotos y se muestra la ultima imagen en la vista previa */
    private fun mostrarFotos() {
        binding.rvFotos.adapter = FotoAdapter(
            rutas,
            {
                elegirDeGaleria.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            { ruta -> confirmarQuitar(ruta) }
        )
        binding.tvContador.text = "Fotos de esta inspección (${rutas.size})"
        val ultima = rutas.lastOrNull()
        if (ultima == null) {
            binding.ivVistaPrevia.setImageDrawable(null)
            binding.tvSinFoto.visibility = View.VISIBLE
        } else {
            binding.tvSinFoto.visibility = View.GONE
            Glide.with(this).load(File(ultima)).centerCrop().into(binding.ivVistaPrevia)
        }
    }

    private fun devolverFotos() {
        val resultado = Intent()
        resultado.putStringArrayListExtra(EXTRA_RUTAS, ArrayList(rutas))
        setResult(Activity.RESULT_OK, resultado)
        finish()
    }

    /** Esta funcion guarda el esatdo actual por si giran la pantalla o el sistema cierra la app con la camara abierta (para no perder el progreso) */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(ESTADO_RUTAS, ArrayList(rutas))
        outState.putStringArrayList(ESTADO_NUEVAS, ArrayList(nuevas))
        outState.putString(ESTADO_PENDIENTE, archivoPendiente?.absolutePath)
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_RUTAS = "rutas"
        private const val ESTADO_RUTAS = "estadoRutas"
        private const val ESTADO_NUEVAS = "estadoNuevas"
        private const val ESTADO_PENDIENTE = "estadoPendiente"
    }
}