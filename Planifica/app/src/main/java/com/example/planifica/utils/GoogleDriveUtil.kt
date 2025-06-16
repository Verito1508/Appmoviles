package com.example.planifica.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.planifica.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

/**
 * Clase utilitaria para interactuar con Google Drive.
 */
object GoogleDriveUtil {

    private const val TAG = "GoogleDriveUtil"
    private const val BACKUP_FILE_NAME = "eventos_respaldo.json"
    private const val REQUEST_SIGN_IN = 1
    
    private var googleSignInClient: GoogleSignInClient? = null
    
    /**
     * Inicia el proceso de respaldo en Google Drive.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de inicio de sesión comenzó correctamente.
     */
    fun iniciarRespaldo(activity: Activity): Boolean {
        return iniciarSesionGoogle(activity)
    }
    
    /**
     * Inicia el proceso de restauración desde Google Drive.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de inicio de sesión comenzó correctamente.
     */
    fun iniciarRestauracion(activity: Activity): Boolean {
        return iniciarSesionGoogle(activity)
    }
    
    /**
     * Inicia el proceso de inicio de sesión en Google.
     * @param activity La actividad desde la que se inicia el proceso.
     * @return true si el proceso de inicio de sesión comenzó correctamente.
     */
    private fun iniciarSesionGoogle(activity: Activity): Boolean {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(activity, signInOptions)
        
        // Limpiar sesiones anteriores
        googleSignInClient?.signOut()
        
        val signInIntent = googleSignInClient?.signInIntent
        if (signInIntent != null) {
            activity.startActivityForResult(signInIntent, REQUEST_SIGN_IN)
            return true
        }
        
        return false
    }
    
    /**
     * Maneja el resultado de la actividad de inicio de sesión.
     * @param requestCode El código de solicitud.
     * @param resultCode El código de resultado.
     * @param data Los datos de la intención.
     * @param activity La actividad que recibe el resultado.
     * @param esRespaldo Indica si es un proceso de respaldo (true) o restauración (false).
     */
    fun manejarResultadoInicioSesion(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        activity: Activity,
        esRespaldo: Boolean
    ) {
        if (requestCode == REQUEST_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Usuario autenticado correctamente
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
                if (account != null) {
                    if (esRespaldo) {
                        respaldarEnDrive(account, activity)
                    } else {
                        restaurarDesdeDrive(account, activity)
                    }
                } else {
                    mostrarError(activity, "No se pudo obtener la cuenta de Google")
                }
            } else {
                mostrarError(activity, "Inicio de sesión cancelado o fallido")
            }
        }
    }
    
    /**
     * Realiza el respaldo en Google Drive.
     * @param account La cuenta de Google autenticada.
     * @param context El contexto de la aplicación.
     */
    private fun respaldarEnDrive(account: GoogleSignInAccount, context: Context) {
        Thread {
            try {
                // Preparar archivo de respaldo
                val archivoRespaldo = RespaldoUtil.prepararArchivoRespaldo(context)
                if (archivoRespaldo == null) {
                    mostrarError(context, "Error al preparar archivo de respaldo")
                    return@Thread
                }
                
                // Obtener servicio de Drive
                val driveService = getDriveService(account, context)
                
                // Buscar si ya existe un archivo de respaldo
                val query = "name = '$BACKUP_FILE_NAME'"
                val result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .execute()
                
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = BACKUP_FILE_NAME
                
                if (result.files.isNotEmpty()) {
                    // Actualizar archivo existente
                    val fileId = result.files[0].id
                    
                    driveService.files().update(
                        fileId,
                        fileMetadata,
                        com.google.api.client.http.FileContent(
                            "application/json",
                            archivoRespaldo
                        )
                    ).execute()
                } else {
                    // Crear nuevo archivo
                    fileMetadata.mimeType = "application/json"
                    
                    driveService.files().create(
                        fileMetadata,
                        com.google.api.client.http.FileContent(
                            "application/json",
                            archivoRespaldo
                        )
                    ).execute()
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
                Log.e(TAG, "Error al respaldar en Drive: ${e.message}")
                mostrarError(context, "Error al respaldar en Drive: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Restaura desde Google Drive.
     * @param account La cuenta de Google autenticada.
     * @param context El contexto de la aplicación.
     */
    private fun restaurarDesdeDrive(account: GoogleSignInAccount, context: Context) {
        Thread {
            try {
                // Obtener servicio de Drive
                val driveService = getDriveService(account, context)
                
                // Buscar archivo de respaldo
                val query = "name = '$BACKUP_FILE_NAME'"
                val result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .execute()
                
                if (result.files.isEmpty()) {
                    mostrarError(context, "No se encontró archivo de respaldo en Drive")
                    return@Thread
                }
                
                // Descargar archivo
                val fileId = result.files[0].id
                val outputStream = ByteArrayOutputStream()
                
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream)
                
                // Guardar en archivo local
                val archivoRespaldo = File(context.filesDir, BACKUP_FILE_NAME)
                FileOutputStream(archivoRespaldo).use { output ->
                    output.write(outputStream.toByteArray())
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
                Log.e(TAG, "Error al restaurar desde Drive: ${e.message}")
                mostrarError(context, "Error al restaurar desde Drive: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Obtiene el servicio de Drive.
     * @param account La cuenta de Google autenticada.
     * @param context El contexto de la aplicación.
     * @return El servicio de Drive.
     */
    private fun getDriveService(account: GoogleSignInAccount, context: Context): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(context.getString(R.string.nombre_app))
            .build()
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
