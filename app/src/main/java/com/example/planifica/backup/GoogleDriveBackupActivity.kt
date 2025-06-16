package com.example.planifica.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.example.planifica.repository.EventoRepository
import java.io.File
import java.io.FileWriter

class GoogleDriveBackupActivity : Activity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val REQUEST_CODE_SIGN_IN = 1001
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
                    createBackupFileAndUpload()
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

    private fun createBackupFileAndUpload() {
        Thread {
            try {
                val eventoRepo = EventoRepository(this)
                val eventos = eventoRepo.obtenerTodosLosEventos()

                val gson = Gson()
                val json = gson.toJson(eventos)

                val backupFile = File(filesDir, "respaldo_planifica.json")
                FileWriter(backupFile).use { writer ->
                    writer.write(json)
                }

                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = "respaldo_planifica.json"

                val mediaContent = FileContent("application/json", backupFile)

                val file = driveService?.files()?.create(fileMetadata, mediaContent)
                    ?.setFields("id")
                    ?.execute()

                runOnUiThread {
                    Toast.makeText(this, "Archivo subido a Drive: ID ${file?.id}", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e("DriveUpload", "Error al subir respaldo", e)
                runOnUiThread {
                    Toast.makeText(this, "Error al subir respaldo: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.start()
    }

}