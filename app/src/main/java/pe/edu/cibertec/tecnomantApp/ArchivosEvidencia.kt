package pe.edu.cibertec.tecnomantApp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * aqui estan los archivos de las fotos de evidencia.
 * Las fotos se guardan en la carpeta privada de la app (files/evidencias), en SQLite solo se guarda su ruta.
 */

object ArchivosEvidencia {
    private const val CARPETA = "evidencias"
    private const val LADO_MAXIMO = 1280
    private const val CALIDAD_JPEG = 80

    /**Aqui Crea un archivo con nombre único donde la cámara escribirá la foto. */
    fun crearArchivo(context: Context): File {
        val carpeta = File(context.filesDir, CARPETA)
        if (!carpeta.exists()) carpeta.mkdirs()
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return File(carpeta, "evidencia_$marca.jpg")
    }

    /** Aqui se genera la URI segura usando el File Provider para que la camara tenga los permisos necesarios para escribir en el archivo */
    fun uriDe(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /**
     * En esta parte se reduce la foto a 1280 px como maximo de lado, corrigiendo su orientacion
     * (si la camara la guardó girada) y la guarda otra vez como JPEG al 80 %. Devuelve false si ocurre un error (por si las moscas)
     */
    fun reducir(archivo: File): Boolean {
        return try {
            val ruta = archivo.absolutePath
            val medidas = BitmapFactory.Options()
            medidas.inJustDecodeBounds = true
            BitmapFactory.decodeFile(ruta, medidas)

            // Se lee la foto a una fracción de su tamaño para no llenar la memoria.
            var fraccion = 1
            while (maxOf(medidas.outWidth, medidas.outHeight) / (fraccion * 2) >= LADO_MAXIMO) {
                fraccion *= 2
            }
            val opciones = BitmapFactory.Options()
            opciones.inSampleSize = fraccion
            var imagen = BitmapFactory.decodeFile(ruta, opciones) ?: return false

            val ladoMayor = maxOf(imagen.width, imagen.height)
            if (ladoMayor > LADO_MAXIMO) {
                val escala = LADO_MAXIMO.toFloat() / ladoMayor
                imagen = Bitmap.createScaledBitmap(
                    imagen, (imagen.width * escala).toInt(), (imagen.height * escala).toInt(), true
                )
            }

            val orientacion = ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            val grados = when (orientacion) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (grados != 0f) {
                val giro = Matrix()
                giro.postRotate(grados)
                imagen = Bitmap.createBitmap(imagen, 0, 0, imagen.width, imagen.height, giro, true)
            }

            FileOutputStream(archivo).use { salida ->
                imagen.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    /** en esta parte solo copia una foto de la galeria a evidencias, devuelve null si falla (por las dudas) */
    fun copiarDesdeUri(context: Context, uri: Uri): File? {
        val destino = crearArchivo(context)
        return try {
            val entrada = context.contentResolver.openInputStream(uri)
            if (entrada == null) {
                destino.delete()
                return null
            }
            entrada.use { origen ->
                FileOutputStream(destino).use { salida -> origen.copyTo(salida) }
            }
            if (reducir(destino)) {
                destino
            } else {
                destino.delete()
                null
            }
        } catch (e: IOException) {
            destino.delete()
            null
        }
    }

    fun borrar(ruta: String) {
        val archivo = File(ruta)
        if (archivo.exists()) archivo.delete()
    }
}