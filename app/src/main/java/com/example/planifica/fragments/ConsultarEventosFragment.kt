package com.example.planifica.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planifica.R
import com.example.planifica.activities.MapaSelectorActivity
import com.example.planifica.adapters.ConsultaEventoAdapter
import com.example.planifica.model.EstadoEvento
import com.example.planifica.model.Evento
import com.example.planifica.model.TipoEvento
import com.example.planifica.repository.EventoRepository
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragmento para consultar, modificar y eliminar eventos.
 */
class ConsultarEventosFragment : Fragment() {
      private lateinit var grupoChipTipoConsulta: com.google.android.material.chip.ChipGroup
    private lateinit var chipPorDia: com.google.android.material.chip.Chip
    private lateinit var chipPorRango: com.google.android.material.chip.Chip
    private lateinit var chipPorMes: com.google.android.material.chip.Chip
    private lateinit var chipPorAno: com.google.android.material.chip.Chip
    
    private lateinit var layoutFiltrosRangoFecha: View
    private lateinit var etFechaInicial: com.google.android.material.textfield.TextInputEditText
    private lateinit var etFechaFinal: com.google.android.material.textfield.TextInputEditText
    private lateinit var layoutFiltroDia: View
    private lateinit var etFechaUnica: com.google.android.material.textfield.TextInputEditText
    private lateinit var layoutFiltrosMesAno: View
    private lateinit var etFiltroAno: com.google.android.material.textfield.TextInputEditText
    private lateinit var tilFiltroMes: com.google.android.material.textfield.TextInputLayout
    private lateinit var actvFiltroMes: android.widget.AutoCompleteTextView
    
    private lateinit var btnConsultar: com.google.android.material.button.MaterialButton
    private lateinit var rvEventos: RecyclerView
    private lateinit var tvSinEventos: TextView
    
    private lateinit var adapter: ConsultaEventoAdapter
    private lateinit var eventoRepository: EventoRepository
    
    private var eventosConsultados: List<Evento> = emptyList()
      override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_consultar_evento, container, false)
        
        // Inicializar vistas
        grupoChipTipoConsulta = root.findViewById(R.id.grupoChipTipoConsulta)
        chipPorDia = root.findViewById(R.id.chipPorDia)
        chipPorRango = root.findViewById(R.id.chipPorRango)
        chipPorMes = root.findViewById(R.id.chipPorMes)
        chipPorAno = root.findViewById(R.id.chipPorAno)
        
        layoutFiltrosRangoFecha = root.findViewById(R.id.layoutFiltrosRangoFecha)
        etFechaInicial = root.findViewById(R.id.etFechaInicial)
        etFechaFinal = root.findViewById(R.id.etFechaFinal)
        layoutFiltroDia = root.findViewById(R.id.layoutFiltroDia)
        etFechaUnica = root.findViewById(R.id.etFechaUnica)
        layoutFiltrosMesAno = root.findViewById(R.id.layoutFiltrosMesAno)
        etFiltroAno = root.findViewById(R.id.etFiltroAno)
        tilFiltroMes = root.findViewById(R.id.tilFiltroMes)
        actvFiltroMes = root.findViewById(R.id.actvFiltroMes)
        
        btnConsultar = root.findViewById(R.id.btnConsultar)
        rvEventos = root.findViewById(R.id.rvEventosConsultados)
        tvSinEventos = root.findViewById(R.id.tvSinEventosConsulta)
        
        return root
    }
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar repository
        eventoRepository = EventoRepository(requireContext())
        
        // Configurar el dropdown de meses
        val meses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val mesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, meses)
        actvFiltroMes.setAdapter(mesAdapter)
        
        // Configurar fecha actual
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        etFechaUnica.setText(dateFormat.format(calendar.time))
        etFechaInicial.setText(dateFormat.format(calendar.time))
        etFechaFinal.setText(dateFormat.format(calendar.time))
        etFiltroAno.setText(calendar.get(Calendar.YEAR).toString())
        actvFiltroMes.setText(meses[calendar.get(Calendar.MONTH)], false)
        
        // Configurar selección de fecha con DatePicker
        configurarSelectoresFecha()
        
        // Configurar cambios en el tipo de consulta
        grupoChipTipoConsulta.setOnCheckedChangeListener { _, checkedId ->
            actualizarVistasConsulta(checkedId)
        }
        
        // Configurar RecyclerView
        rvEventos.layoutManager = LinearLayoutManager(context)
        adapter = ConsultaEventoAdapter(
            emptyList(),
            onVerDetallesClick = { evento -> mostrarDetallesEvento(evento) },
            onEditarClick = { evento -> abrirEdicionEvento(evento) },
            onEliminarClick = { evento -> mostrarDialogoConfirmarEliminar(evento) },
            onVerUbicacionClick = { evento -> mostrarUbicacionEvento(evento) },
            onContactoClick = { evento -> verInfoContacto(evento) }
        )
        rvEventos.adapter = adapter
        
        // Configurar botón de consulta
        btnConsultar.setOnClickListener { realizarConsulta() }
        
        // Mostrar inicialmente la vista de consulta por rango (que es la seleccionada por defecto)
        actualizarVistasConsulta(R.id.chipPorRango)
    }
      private fun configurarSelectoresFecha() {
        val calendar = Calendar.getInstance()
        
        val onDateSetListener = { etFechaTarget: TextInputEditText ->
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etFechaTarget.setText(dateFormat.format(calendar.time))
            }
        }
        
        etFechaUnica.setOnClickListener {
            mostrarDatePicker(etFechaUnica, onDateSetListener)
        }
        
        etFechaInicial.setOnClickListener {
            mostrarDatePicker(etFechaInicial, onDateSetListener)
        }
        
        etFechaFinal.setOnClickListener {
            mostrarDatePicker(etFechaFinal, onDateSetListener)
        }
    }
    
    private fun mostrarDatePicker(
        etFechaTarget: TextInputEditText,
        onDateSetListener: (TextInputEditText) -> DatePickerDialog.OnDateSetListener
    ) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        try {
            val fechaActual = etFechaTarget.text.toString()
            if (fechaActual.isNotEmpty()) {
                calendar.time = dateFormat.parse(fechaActual) ?: calendar.time
            }
        } catch (e: Exception) {
            // Si hay un error al parsear la fecha, usar la fecha actual
        }
        
        DatePickerDialog(
            requireContext(),
            onDateSetListener(etFechaTarget),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
      private fun actualizarVistasConsulta(checkedId: Int) {
        // Ocultar todas las vistas
        layoutFiltroDia.visibility = View.GONE
        layoutFiltrosRangoFecha.visibility = View.GONE
        layoutFiltrosMesAno.visibility = View.GONE
        tilFiltroMes.visibility = View.GONE
        
        // Mostrar la vista correspondiente
        when (checkedId) {
            R.id.chipPorDia -> {
                layoutFiltroDia.visibility = View.VISIBLE
            }
            R.id.chipPorRango -> {
                layoutFiltrosRangoFecha.visibility = View.VISIBLE
            }
            R.id.chipPorMes -> {
                layoutFiltrosMesAno.visibility = View.VISIBLE
                tilFiltroMes.visibility = View.VISIBLE
            }
            R.id.chipPorAno -> {
                layoutFiltrosMesAno.visibility = View.VISIBLE
                tilFiltroMes.visibility = View.GONE
            }
        }
    }
      private fun realizarConsulta() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        try {
            eventosConsultados = when (grupoChipTipoConsulta.checkedChipId) {
                R.id.chipPorDia -> {
                    val fecha = dateFormat.parse(etFechaUnica.text.toString()) ?: Date()
                    eventoRepository.obtenerEventosPorFecha(fecha)
                }
                R.id.chipPorRango -> {
                    val fechaInicial = dateFormat.parse(etFechaInicial.text.toString()) ?: Date()
                    val fechaFinal = dateFormat.parse(etFechaFinal.text.toString()) ?: Date()
                    eventoRepository.obtenerEventosPorRangoFechas(fechaInicial, fechaFinal)
                }
                R.id.chipPorMes -> {
                    val mesTexto = actvFiltroMes.text.toString()
                    val meses = arrayOf(
                        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
                    )
                    val mes = meses.indexOf(mesTexto) + 1 // Meses son 1-12
                    val ano = etFiltroAno.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                    eventoRepository.obtenerEventosPorMes(mes, ano)
                }
                R.id.chipPorAno -> {
                    val ano = etFiltroAno.text.toString().toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
                    eventoRepository.obtenerEventosPorAño(ano)
                }
                else -> emptyList()
            }
            
            // Actualizar la UI
            if (eventosConsultados.isNotEmpty()) {
                adapter.actualizarDatos(eventosConsultados)
                rvEventos.visibility = View.VISIBLE
                tvSinEventos.visibility = View.GONE
            } else {
                rvEventos.visibility = View.GONE
                tvSinEventos.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            rvEventos.visibility = View.GONE
            tvSinEventos.visibility = View.VISIBLE
        }
    }
    
    private fun mostrarDetallesEvento(evento: Evento) {
        // Actualizar para mostrar los detalles quiza en un
        // alert dialog o en un inflater
    }
    
    fun mostrarDialogoActualizarEstado(evento: Evento) {
        val opciones = resources.getStringArray(R.array.opciones_estado_evento)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_elegir_estado)
            .setItems(opciones) { _, which ->
                val nuevoEstado = when (which) {
                    0 -> EstadoEvento.PENDIENTE
                    1 -> EstadoEvento.REALIZADO
                    2 -> EstadoEvento.APLAZADO
                    else -> evento.estado
                }
                
                if (nuevoEstado != evento.estado) {
                    actualizarEstadoEvento(evento, nuevoEstado)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
    
    private fun actualizarEstadoEvento(evento: Evento, nuevoEstado: EstadoEvento) {
        if (eventoRepository.actualizarEstadoEvento(evento.id, nuevoEstado)) {
            // Actualizar el evento en la lista
            val eventoActualizado = evento.copy(estado = nuevoEstado)
            val nuevaLista = eventosConsultados.toMutableList().apply {
                val index = indexOfFirst { it.id == evento.id }
                if (index != -1) {
                    set(index, eventoActualizado)
                }
            }
            
            eventosConsultados = nuevaLista
            adapter.actualizarDatos(nuevaLista)
        }
    }
    
    private fun mostrarDialogoConfirmarEliminar(evento: Evento) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.titulo_confirmar_eliminar_evento)
            .setMessage(R.string.mensaje_confirmar_eliminar_evento)
            .setPositiveButton(R.string.si) { _, _ ->
                eliminarEvento(evento)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun eliminarEvento(evento: Evento) {
        if (eventoRepository.eliminarEvento(evento.id)) {
            // Eliminar el evento de la lista
            val nuevaLista = eventosConsultados.toMutableList().apply {
                removeAll { it.id == evento.id }
            }
            
            eventosConsultados = nuevaLista
            
            if (nuevaLista.isEmpty()) {
                rvEventos.visibility = View.GONE
                tvSinEventos.visibility = View.VISIBLE
            } else {
                adapter.actualizarDatos(nuevaLista)
            }
        }
    }
    
    // Implementación de la interfaz de edición
    inner class EventoEditListener : EditarEventoFragment.OnEventoEditadoListener {
        override fun onEventoEditado(evento: Evento) {
            // Actualizar el evento en la lista
            val nuevaLista = eventosConsultados.toMutableList().apply {
                val index = indexOfFirst { it.id == evento.id }
                if (index != -1) {
                    set(index, evento)
                }
            }
            
            eventosConsultados = nuevaLista
            adapter.actualizarDatos(nuevaLista)
        }
        
        override fun onEventoEliminado(eventoId: Long) {
            // Eliminar el evento de la lista
            val nuevaLista = eventosConsultados.toMutableList().apply {
                removeAll { it.id == eventoId }
            }
            
            eventosConsultados = nuevaLista
            
            if (nuevaLista.isEmpty()) {
                rvEventos.visibility = View.GONE
                tvSinEventos.visibility = View.VISIBLE
            } else {
                adapter.actualizarDatos(nuevaLista)
            }
        }
    }
    
    private fun abrirEdicionEvento(evento: Evento) {
        val editarEventoFragment = EditarEventoFragment.newInstance(evento)
        editarEventoFragment.setOnEventoEditadoListener(EventoEditListener())
        
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmento_contenido_principal, editarEventoFragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun mostrarUbicacionEvento(evento: Evento) {
        evento.ubicacion?.let { ubicacion ->
            val intent = Intent(requireActivity(), MapaSelectorActivity::class.java)
            // Añadir coordenadas de la ubicación al intent
            intent.putExtra("latitud", ubicacion.latitud)
            intent.putExtra("longitud", ubicacion.longitud)
            intent.putExtra("direccion", ubicacion.direccion)
            intent.putExtra("soloVisualizacion", true) // Modo solo visualización
            startActivity(intent)
        }
    }
    
    private fun verInfoContacto(evento: Evento) {
        evento.contacto?.let { contacto ->
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.contacto)
                .setMessage("${contacto.nombre}\n${contacto.telefono}")
                .setPositiveButton(R.string.aceptar, null)
                .show()
        }
    }
}