package com.example.planifica.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.example.planifica.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Clase utilitaria para interactuar con Dropbox.
 */
object DropboxUtil {
    
    private const val TAG = "DropboxUtil"
    private const val BACKUP_FILE_PATH = "/eventos_respaldo.json"
    private const val APP_KEY = "y3xur6y63adxn2j"
    
    private var dropboxClient: DbxClientV2? = null
    private var esRespaldo = false
    var seSolicitoAutenticacion = false


    /**
     * Inicia el proceso de respaldo en Dropbox.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de autenticación comenzó correctamente.
     */
    fun iniciarRespaldo(activity: Activity): Boolean {
        seSolicitoAutenticacion = true
        esRespaldo = true
        return iniciarSesionDropbox(activity)
    }
    
    /**
     * Inicia el proceso de restauración desde Dropbox.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de autenticación comenzó correctamente.
     */
    fun iniciarRestauracion(activity: Activity): Boolean {
        seSolicitoAutenticacion = true
        esRespaldo = false
        return iniciarSesionDropbox(activity)
    }
    
    /**
     * Inicia el proceso de autenticación en Dropbox.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de autenticación comenzó correctamente.
     */
    private fun iniciarSesionDropbox(activity: Activity): Boolean {
        Auth.startOAuth2Authentication(activity, APP_KEY)
        return true
    }
    
    /**
     * Continúa el proceso después de la autenticación.
     * @param activity La actividad desde la que se continúa el proceso.
     */
    fun continuarDespuesDeAutenticacion(activity: Activity) {
        val accessToken = Auth.getOAuth2Token()
        Log.d(TAG, "AccessToken: $accessToken")

        if (accessToken == null) {
            mostrarError(activity, "Error en la autenticación con Dropbox")
            return
        }
        
        // Configurar cliente de Dropbox
        val config = DbxRequestConfig("OrganizadorTareas/1.0")
        dropboxClient = DbxClientV2(config, accessToken)
        
        // Continuar con la operación correspondiente
        if (esRespaldo) {
            respaldarEnDropbox(activity)
        } else {
            restaurarDesdeDropbox(activity)
        }
    }
    
    /**
     * Realiza el respaldo en Dropbox.
     * @param context El contexto de la aplicación.
     */
    private fun respaldarEnDropbox(context: Context) {
        Thread {
            try {
                // Preparar archivo de respaldo
                val archivoRespaldo = RespaldoUtil.prepararArchivoRespaldo(context)
                if (archivoRespaldo == null) {
                    mostrarError(context, "Error al preparar archivo de respaldo")
                    return@Thread
                }
                
                // Subir a Dropbox
                if (dropboxClient == null) {
                    mostrarError(context, "Cliente de Dropbox no inicializado")
                    return@Thread
                }
                FileInputStream(archivoRespaldo).use { inputStream ->
                    dropboxClient?.files()?.uploadBuilder(BACKUP_FILE_PATH)
                        ?.withMode(WriteMode.OVERWRITE)
                        ?.uploadAndFinish(inputStream)
                }
                
                // Mostrar mensaje de éxito
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(
                        context,
                        R.string.respaldo_exitoso,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al respaldar en Dropbox: ${e.message}")
                mostrarError(context, "Error al respaldar en Dropbox: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Restaura desde Dropbox.
     * @param context El contexto de la aplicación.
     */
    private fun restaurarDesdeDropbox(context: Context) {
        Thread {
            try {
                // Descargar desde Dropbox
                val archivoRespaldo = File(context.filesDir, "eventos_respaldo.json")
                
                FileOutputStream(archivoRespaldo).use { outputStream ->
                    dropboxClient?.files()?.download(BACKUP_FILE_PATH)
                        ?.download(outputStream)
                }
                
                // Restaurar desde archivo local
                if (RespaldoUtil.restaurarLocal(context)) {
                    // Mostrar mensaje de éxito
                    (context as? Activity)?.runOnUiThread {
                        Toast.makeText(
                            context,
                            R.string.restauracion_exitosa,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    mostrarError(context, "Error al restaurar desde archivo local")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al restaurar desde Dropbox: ${e.message}")
                mostrarError(context, "Error al restaurar desde Dropbox: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Muestra un mensaje de error.
     * @param context El contexto de la aplicación.
     * @param mensaje El mensaje de error.
     */
    private fun mostrarError(context: Context, mensaje: String) {
        Log.e(TAG, mensaje)
        (context as? Activity)?.runOnUiThread {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }
    }
}
