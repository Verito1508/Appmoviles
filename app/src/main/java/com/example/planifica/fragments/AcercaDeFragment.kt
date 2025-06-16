package com.example.planifica.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.planifica.R

/**
 * Fragmento para mostrar información sobre la aplicación.
 */
class AcercaDeFragment : Fragment() {
      private lateinit var tvVersion: TextView
    private lateinit var tvDesarrolladores: TextView
    private lateinit var tvDescripcion: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_sobre, container, false)
        
        tvVersion = root.findViewById(R.id.tvVersionAppAcercaDe)
        tvDesarrolladores = root.findViewById(R.id.tvDesarrolladoPor)
        tvDescripcion = root.findViewById(R.id.tvDescripcionAppAcercaDe)
        
        return root
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar información de versión usando el PackageInfo
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionText = getString(R.string.plantilla_version, packageInfo.versionName)
        tvVersion.text = versionText
        
        // Configurar información de desarrolladores
        tvDesarrolladores.text = getString(R.string.desarrollado_por)
        
        // Configurar descripción de la aplicación
        tvDescripcion.text = getString(R.string.texto_descripcion_app)
    }
}
