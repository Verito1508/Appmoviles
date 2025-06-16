package com.example.planifica.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.planifica.model.Evento
import com.example.planifica.repository.EventoRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.media.MediaHttpDownloader
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class GoogleDriveRestoreActivity : Activity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val REQUEST_CODE_SIGN_IN = 2001
    private var driveService: Drive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signInToGoogle()
    }

    private fun signInToGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                task.result?.let { account ->
                    driveService = getDriveService(account)
                    restoreBackupFromDrive()
                }
            } else {
                Toast.makeText(this, "Error al iniciar sesión en Google", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Planifica").build()
    }

    private fun restoreBackupFromDrive() {
        Thread {
            try {
                val files = driveService?.files()?.list()
                    ?.setQ("name = 'respaldo_planifica.json'")
                    ?.setFields("files(id, name)")
                    ?.execute()

                val file = files?.files?.firstOrNull()

                if (file != null) {
                    val outputFile = File(cacheDir, "respaldo_tmp.json")
                    val outputStream = FileOutputStream(outputFile)
                    driveService?.files()?.get(file.id)
                        ?.executeMediaAndDownloadTo(outputStream)
                    outputStream.close()

                    val eventosJson = InputStreamReader(outputFile.inputStream())
                    val eventos = Gson().fromJson(eventosJson, Array<Evento>::class.java)

                    val repo = EventoRepository(this)
                    eventos.forEach { evento -> repo.guardarEvento(evento) }

                    runOnUiThread {
                        Toast.makeText(this, "Restauración completa", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Archivo de respaldo no encontrado", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("DriveRestore", "Error al restaurar", e)
                runOnUiThread {
                    Toast.makeText(this, "Error al restaurar: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.start()
    }
}