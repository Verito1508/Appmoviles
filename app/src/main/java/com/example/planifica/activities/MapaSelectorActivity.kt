package com.example.planifica.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.planifica.R
import com.example.planifica.model.Ubicacion
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Actividad para seleccionar una ubicación en el mapa.
 */
class MapaSelectorActivity : AppCompatActivity(), OnMapReadyCallback {
      private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private lateinit var tvDireccion: TextView
    private lateinit var btnSeleccionarUbicacion: Button
    private lateinit var fabMiUbicacion: FloatingActionButton
    
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""
    private var soloVisualizacion: Boolean = false
    
    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 100
        const val EXTRA_UBICACION = "extra_ubicacion"
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_selector)
        
        // Obtener datos del intent
        soloVisualizacion = intent.getBooleanExtra("soloVisualizacion", false)
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)
        val direccion = intent.getStringExtra("direccion")
        
        // Configurar ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (soloVisualizacion) {
            getString(R.string.ver_ubicacion)
        } else {
            getString(R.string.seleccionar_ubicacion)
        }
        
        // Inicializar vistas
        tvDireccion = findViewById(R.id.tvDireccion)
        btnSeleccionarUbicacion = findViewById(R.id.btnSeleccionarUbicacion)
        fabMiUbicacion = findViewById(R.id.fabMiUbicacion)
        
        // Configurar modo visualización
        if (soloVisualizacion) {
            btnSeleccionarUbicacion.visibility = View.GONE
            if (latitud != 0.0 && longitud != 0.0) {
                selectedLatLng = LatLng(latitud, longitud)
                selectedAddress = direccion ?: "${latitud}, ${longitud}"
            }
        }
        
        // Inicializar el proveedor de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Inicializar el fragmento de mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        // Configurar botones
        btnSeleccionarUbicacion.setOnClickListener {
            seleccionarUbicacion()
        }
        
        fabMiUbicacion.setOnClickListener {
            obtenerUbicacionActual()
        }
    }
      override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Configurar el mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        
        if (soloVisualizacion) {
            // En modo visualización, solo mostrar la ubicación existente
            selectedLatLng?.let { latLng ->
                marcarUbicacion(latLng)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                tvDireccion.text = selectedAddress
                tvDireccion.visibility = View.VISIBLE
            }
        } else {
            // En modo selección, permitir clics en el mapa
            mMap.setOnMapClickListener { latLng ->
                marcarUbicacion(latLng)
            }
            
            // Obtener la ubicación actual al iniciar
            obtenerUbicacionActual()
        }
    }
    
    private fun obtenerUbicacionActual() {
        // Verificar permisos
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
            return
        }
        
        // Habilitar capa de ubicación en el mapa
        mMap.isMyLocationEnabled = true
        
        // Obtener la ubicación actual
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                marcarUbicacion(currentLatLng)
            }
        }
    }
    
    private fun marcarUbicacion(latLng: LatLng) {
        // Guardar la ubicación seleccionada
        selectedLatLng = latLng
        
        // Limpiar marcador anterior si existe
        currentMarker?.remove()
        
        // Agregar nuevo marcador
        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.ubicacion_seleccionada))
        )
        
        // Mover la cámara a la ubicación seleccionada
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        
        // Obtener dirección a partir de coordenadas (geocodificación inversa)
        obtenerDireccion(latLng)
    }
    
    private fun obtenerDireccion(latLng: LatLng) {
        // Utilizar directamnnte las coordenadas, actualizar -> usar direccion
        selectedAddress = "${latLng.latitude}, ${latLng.longitude}"
        tvDireccion.text = selectedAddress
        tvDireccion.visibility = View.VISIBLE
        btnSeleccionarUbicacion.visibility = View.VISIBLE
    }
    
    private fun seleccionarUbicacion() {
        selectedLatLng?.let { latLng ->
            val ubicacion = Ubicacion(
                latitud = latLng.latitude,
                longitud = latLng.longitude,
                direccion = selectedAddress
            )
            
            val intent = Intent().apply {
                putExtra(EXTRA_UBICACION, ubicacion)
            }
            
            setResult(RESULT_OK, intent)
            finish()
        } ?: run {
            Toast.makeText(
                this,
                getString(R.string.seleccionar_ubicacion_en_mapa),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener ubicación
                obtenerUbicacionActual()
            } else {
                // Permiso denegado
                Toast.makeText(
                    this,
                    getString(R.string.permiso_ubicacion_requerido),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Usar una ubicación por defecto
                val defaultLocation = LatLng(19.4326, -99.1332) // Ciudad de México
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
