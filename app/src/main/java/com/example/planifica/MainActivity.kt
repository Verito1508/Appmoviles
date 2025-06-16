package com.example.planifica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.planifica.fragments.AcercaDeFragment
import com.example.planifica.fragments.AgregarEventoFragment
import com.example.planifica.fragments.ConsultarEventosFragment
import com.example.planifica.fragments.InicioFragment
import com.example.planifica.utils.RespaldoUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.planifica.utils.DropboxUtil

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    
    private val PERMISSIONS_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Configurar barra de herramientas
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.barra_herramientas)
        setSupportActionBar(toolbar)
        
        // Configurar Navigation Drawer
        drawerLayout = findViewById(R.id.layout_cajon_navegacion)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Configurar navegación
        val navView = findViewById<NavigationView>(R.id.vista_navegacion)
        navView.setNavigationItemSelectedListener(this)
        
        // Configurar navegación inferior
        val bottomNavView = findViewById<BottomNavigationView>(R.id.vista_navegacion_inferior)
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio_inferior -> {
                    replaceFragment(InicioFragment())
                    true
                }
                R.id.nav_consultar_inferior -> {
                    replaceFragment(ConsultarEventosFragment())
                    true
                }
                R.id.nav_salir_inferior -> {
                    mostrarDialogoConfirmarSalir()
                    true
                }
                else -> false
            }
        }
        
        // Mostrar fragmento de inicio por defecto
        if (savedInstanceState == null) {
            replaceFragment(InicioFragment())
        }
        
        // Solicitar permisos necesarios
        solicitarPermisos()
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_agregar_evento -> {
                replaceFragment(AgregarEventoFragment())
            }
            R.id.nav_consultar_modificar_eventos -> {
                replaceFragment(ConsultarEventosFragment())
            }
            R.id.nav_respaldo -> {
                mostrarDialogoRespaldo()
            }
            R.id.nav_restaurar -> {
                mostrarDialogoRestaurar()
            }
            R.id.nav_acerca_de -> {
                replaceFragment(AcercaDeFragment())
            }
            R.id.nav_salir -> {
                mostrarDialogoConfirmarSalir()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmento_contenido_principal, fragment)
            .commit()
    }
    
    private fun mostrarDialogoRespaldo() {
        val opciones = arrayOf(
            getString(R.string.respaldar_en_dropbox),
            getString(R.string.respaldar_en_google_drive)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.seleccionar_servicio_respaldo))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> respaldoEnDropbox()
                    1 -> respaldoEnGoogleDrive()
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }
    
    private fun mostrarDialogoRestaurar() {
        val opciones = arrayOf(
            getString(R.string.restaurar_de_dropbox),
            getString(R.string.restaurar_de_google_drive)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.seleccionar_servicio_restauracion))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> restaurarDeDropbox()
                    1 -> restaurarDeGoogleDrive()
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun respaldoEnDropbox() {
        val iniciado = DropboxUtil.iniciarRespaldo(this)
        if (!iniciado) {
            Toast.makeText(this, "No se pudo iniciar el respaldo en Dropbox", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun respaldoEnGoogleDrive() {
        // Implementación del respaldo en Google Drive
        // Esto requerirá la integración con la API de Google Drive
        Toast.makeText(this, "Función de respaldo en Google Drive no implementada", Toast.LENGTH_SHORT).show()
    }

    private fun restaurarDeDropbox() {
        val iniciado = DropboxUtil.iniciarRestauracion(this)
        if (!iniciado) {
            Toast.makeText(this, "No se pudo iniciar la restauración desde Dropbox", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun restaurarDeGoogleDrive() {
        // Implementación de la restauración desde Google Drive
        Toast.makeText(this, "Función de restauración desde Google Drive no implementada", Toast.LENGTH_SHORT).show()
    }
    
    private fun mostrarDialogoConfirmarSalir() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.nav_salir))
            .setMessage("¿Estás seguro de que deseas salir de la aplicación?")
            .setPositiveButton(getString(R.string.si)) { _, _ -> finish() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun solicitarPermisos() {
        val permisosNecesarios = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        )
        
        val permisosNoOtorgados = permisosNecesarios.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permisosNoOtorgados.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisosNoOtorgados, PERMISSIONS_REQUEST_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val permisosRechazados = grantResults.indices
                .filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
                .map { permissions[it] }
            
            if (permisosRechazados.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Algunas funcionalidades pueden estar limitadas debido a permisos no otorgados",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (DropboxUtil.seSolicitoAutenticacion) {
            DropboxUtil.seSolicitoAutenticacion = false
            DropboxUtil.continuarDespuesDeAutenticacion(this)
        }
    }



    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}