package com.example.planifica.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planifica.R
import com.example.planifica.adapters.EventoAdapter
import com.example.planifica.model.Evento
import com.example.planifica.repository.EventoRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragmento para la pantalla de inicio que muestra los eventos del día y próximos.
 */
class InicioFragment : Fragment() {
    
    private lateinit var tvEventosHoy: TextView
    private lateinit var rvEventosHoy: RecyclerView
    private lateinit var tvEventosProximos: TextView
    private lateinit var rvEventosProximos: RecyclerView
    private lateinit var tvSinEventosInicio: TextView
    
    private lateinit var eventoRepository: EventoRepository
    private lateinit var eventosHoyAdapter: EventoAdapter
    private lateinit var eventosProximosAdapter: EventoAdapter
      override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragmet_inicio, container, false)
        
        tvEventosHoy = root.findViewById(R.id.tvTituloEventosHoy)
        rvEventosHoy = root.findViewById(R.id.rvEventosHoy)
        tvEventosProximos = root.findViewById(R.id.tvTituloEventosProximos)
        rvEventosProximos = root.findViewById(R.id.rvEventosProximos)
        tvSinEventosInicio = root.findViewById(R.id.tvSinEventosInicio)
        
        // Inicializar repository
        eventoRepository = EventoRepository(requireContext())
        
        // Configurar RecyclerViews
        rvEventosHoy.layoutManager = LinearLayoutManager(context)
        rvEventosProximos.layoutManager = LinearLayoutManager(context)
        
        // Configurar adaptadores
        eventosHoyAdapter = EventoAdapter(emptyList()) { evento ->
            // Manejar clic en evento de hoy
            mostrarDetallesEvento(evento)
        }
        
        eventosProximosAdapter = EventoAdapter(emptyList()) { evento ->
            // Manejar clic en evento próximo
            mostrarDetallesEvento(evento)
        }
        
        rvEventosHoy.adapter = eventosHoyAdapter
        rvEventosProximos.adapter = eventosProximosAdapter
        
        return root
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Cargar eventos
        cargarEventos()
    }
      private fun cargarEventos() {
        // Obtener eventos de hoy
        val eventosHoy = eventoRepository.obtenerEventosHoy()
        
        // Obtener eventos próximos (a partir de mañana)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val manana = calendar.time
        
        calendar.add(Calendar.MONTH, 1)
        val enUnMes = calendar.time
        
        val eventosProximos = eventoRepository.obtenerEventosPorRangoFechas(manana, enUnMes)
        
        // Actualizar UI para eventos de hoy
        if (eventosHoy.isNotEmpty()) {
            eventosHoyAdapter.actualizarDatos(eventosHoy)
            tvEventosHoy.visibility = View.VISIBLE
            rvEventosHoy.visibility = View.VISIBLE
        } else {
            rvEventosHoy.visibility = View.GONE
        }
        
        // Actualizar UI para eventos próximos
        if (eventosProximos.isNotEmpty()) {
            eventosProximosAdapter.actualizarDatos(eventosProximos)
            tvEventosProximos.visibility = View.VISIBLE
            rvEventosProximos.visibility = View.VISIBLE
        } else {
            rvEventosProximos.visibility = View.GONE
        }
        
        // Mostrar mensaje si no hay eventos
        if (eventosHoy.isEmpty() && eventosProximos.isEmpty()) {
            tvSinEventosInicio.visibility = View.VISIBLE
        } else {
            tvSinEventosInicio.visibility = View.GONE
        }
    }
    
    private fun mostrarDetallesEvento(evento: Evento) {
        // Implementar vista de detalles de evento
        // Esto podría ser un diálogo o un nuevo fragmento
    }
    
    override fun onResume() {
        super.onResume()
        // Recargar eventos al volver al fragmento
        cargarEventos()
    }
}
