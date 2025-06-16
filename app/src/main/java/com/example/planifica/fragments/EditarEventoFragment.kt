package com.example.planifica.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.planifica.R
import com.example.planifica.activities.MapaSelectorActivity
import com.example.planifica.model.Contacto
import com.example.planifica.model.EstadoEvento
import com.example.planifica.model.Evento
import com.example.planifica.model.TipoEvento
import com.example.planifica.model.TipoRecordatorio
import com.example.planifica.model.Ubicacion
import com.example.planifica.repository.EventoRepository
import com.example.planifica.utils.RecordatorioUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Fragmento para editar eventos existentes.
 */
class EditarEventoFragment : Fragment() {
    
    private lateinit var btnCategoryCita: MaterialButton
    private lateinit var btnCategoryJunta: MaterialButton
    private lateinit var btnCategoryEntrega: MaterialButton
    private lateinit var btnCategoryExamen: MaterialButton
    private lateinit var btnCategoryOtro: MaterialButton
    
    private lateinit var tilDate: TextInputLayout
    private lateinit var etDate: TextInputEditText
    private lateinit var tilTime: TextInputLayout
    private lateinit var etTime: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var tilStatus: TextInputLayout
    private lateinit var actvStatus: AutoCompleteTextView
    private lateinit var tilLocation: TextInputLayout
    private lateinit var etLocation: TextInputEditText
    private lateinit var tilContact: TextInputLayout
    private lateinit var etContact: TextInputEditText
    private lateinit var tilReminder: TextInputLayout
    private lateinit var actvReminder: AutoCompleteTextView
    private lateinit var btnSaveEvent: Button
    private lateinit var btnDeleteEvent: Button
    
    private var selectedCategory: TipoEvento = TipoEvento.CITA
    private var selectedDate: Date = Calendar.getInstance().time
    private var selectedTime: String = ""
    private var selectedContacto: Contacto? = null
    private var selectedUbicacion: Ubicacion? = null
    
    private val eventoRepository by lazy { EventoRepository(requireContext()) }
    
    // El evento a editar
    private lateinit var evento: Evento
    
    // Interfaz para comunicarse con el fragmento padre
    interface OnEventoEditadoListener {
        fun onEventoEditado(evento: Evento)
        fun onEventoEliminado(eventoId: Long)
    }
    
    private var listener: OnEventoEditadoListener? = null
    
    fun setOnEventoEditadoListener(listener: OnEventoEditadoListener) {
        this.listener = listener
    }
    
    companion object {
        private const val ARG_EVENTO = "evento"
        
        fun newInstance(evento: Evento): EditarEventoFragment {
            val fragment = EditarEventoFragment()
            val args = Bundle()
            args.putParcelable(ARG_EVENTO, evento)
            fragment.arguments = args
            return fragment
        }
    }
    
    private val contactoPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                val projection = arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                )
                
                requireContext().contentResolver.query(
                    contactUri, projection, null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        
                        val contactId = cursor.getString(idIndex)
                        val contactName = cursor.getString(nameIndex)
                        
                        selectedContacto = Contacto(
                            id = contactId,
                            nombre = contactName
                        )
                        
                        etContact.setText(contactName)
                    }
                }
            }
        }
    }
    
    private val ubicacionPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getParcelableExtra<Ubicacion>(MapaSelectorActivity.EXTRA_UBICACION)?.let { ubicacion ->
                selectedUbicacion = ubicacion
                etLocation.setText(ubicacion.direccion)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            evento = it.getParcelable(ARG_EVENTO) ?: throw IllegalArgumentException("Se requiere un evento para editar")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_editar_evento, container, false)
        
        // Inicializar vistas
        btnCategoryCita = root.findViewById(R.id.btnCategoryCita)
        btnCategoryJunta = root.findViewById(R.id.btnCategoryJunta)
        btnCategoryEntrega = root.findViewById(R.id.btnCategoryEntrega)
        btnCategoryExamen = root.findViewById(R.id.btnCategoryExamen)
        btnCategoryOtro = root.findViewById(R.id.btnCategoryOtro)
        
        tilDate = root.findViewById(R.id.tilDate)
        etDate = root.findViewById(R.id.etDate)
        tilTime = root.findViewById(R.id.tilTime)
        etTime = root.findViewById(R.id.etTime)
        etDescription = root.findViewById(R.id.etDescription)
        tilStatus = root.findViewById(R.id.tilStatus)
        actvStatus = root.findViewById(R.id.actvStatus)
        tilLocation = root.findViewById(R.id.tilLocation)
        etLocation = root.findViewById(R.id.etLocation)
        tilContact = root.findViewById(R.id.tilContact)
        etContact = root.findViewById(R.id.etContact)
        tilReminder = root.findViewById(R.id.tilReminder)
        actvReminder = root.findViewById(R.id.actvReminder)
        btnSaveEvent = root.findViewById(R.id.btnSaveEvent)
        btnDeleteEvent = root.findViewById(R.id.btnDeleteEvent)
        
        return root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar botones de categoría
        configurarBotonesCategorias()
        
        // Configurar campos de fecha y hora
        configurarCamposFechaHora()
        
        // Configurar dropdowns
        configurarDropdowns()
        
        // Configurar selectores de ubicación y contacto
        configurarSelectores()
        
        // Cargar datos del evento
        cargarDatosEvento()
        
        // Configurar botón de guardar
        btnSaveEvent.setOnClickListener { guardarEvento() }
        
        // Configurar botón de eliminar
        btnDeleteEvent.setOnClickListener { confirmarEliminarEvento() }
    }
    
    private fun configurarBotonesCategorias() {
        val allButtons = listOf(
            btnCategoryCita, btnCategoryJunta, btnCategoryEntrega,
            btnCategoryExamen, btnCategoryOtro
        )
        
        fun actualizarSeleccion(seleccionado: MaterialButton, categoria: TipoEvento) {
            allButtons.forEach { it.isChecked = it == seleccionado }
            selectedCategory = categoria
        }
        
        btnCategoryCita.setOnClickListener { 
            actualizarSeleccion(btnCategoryCita, TipoEvento.CITA)
        }
        
        btnCategoryJunta.setOnClickListener { 
            actualizarSeleccion(btnCategoryJunta, TipoEvento.JUNTA)
        }
        
        btnCategoryEntrega.setOnClickListener { 
            actualizarSeleccion(btnCategoryEntrega, TipoEvento.ENTREGA_PROYECTO)
        }
        
        btnCategoryExamen.setOnClickListener { 
            actualizarSeleccion(btnCategoryExamen, TipoEvento.EXAMEN)
        }
        
        btnCategoryOtro.setOnClickListener { 
            actualizarSeleccion(btnCategoryOtro, TipoEvento.OTRO)
        }
    }
    
    private fun configurarCamposFechaHora() {
        // Configurar campo de fecha
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        etDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                selectedDate = calendar.time
                etDate.setText(dateFormat.format(calendar.time))
            }, year, month, day).show()
        }
        
        // Configurar campo de hora
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        etTime.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            
            TimePickerDialog(requireContext(), { _, h, m ->
                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)
                selectedTime = timeFormat.format(calendar.time)
                etTime.setText(selectedTime)
            }, hour, minute, true).show()
        }
    }
    
    private fun configurarDropdowns() {
        // Configurar dropdown de estado
        val estadosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.opciones_estado_evento)
        )
        actvStatus.setAdapter(estadosAdapter)
        
        // Configurar dropdown de recordatorio
        val recordatoriosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.opciones_recordatorio)
        )
        actvReminder.setAdapter(recordatoriosAdapter)
    }
    
    private fun configurarSelectores() {
        // Configurar selector de ubicación
        etLocation.setOnClickListener {
            // Verificar permisos de ubicación
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Lanzar la actividad del selector de mapa
                val intent = Intent(requireActivity(), MapaSelectorActivity::class.java)
                ubicacionPicker.launch(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permiso_ubicacion_requerido),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Configurar selector de contacto
        etContact.setOnClickListener {
            // Verificar permisos de contactos
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                contactoPicker.launch(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permiso_ubicacion_requerido),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun cargarDatosEvento() {
        // Establecer la categoría
        when (evento.categoria) {
            TipoEvento.CITA -> btnCategoryCita.performClick()
            TipoEvento.JUNTA -> btnCategoryJunta.performClick()
            TipoEvento.ENTREGA_PROYECTO -> btnCategoryEntrega.performClick()
            TipoEvento.EXAMEN -> btnCategoryExamen.performClick()
            TipoEvento.OTRO -> btnCategoryOtro.performClick()
        }
        
        // Establecer fecha y hora
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(evento.fecha))
        selectedDate = evento.fecha
        
        etTime.setText(evento.hora)
        selectedTime = evento.hora
        
        // Establecer descripción
        etDescription.setText(evento.descripcion)
        
        // Establecer estado
        val estadoStr = when (evento.estado) {
            EstadoEvento.PENDIENTE -> getString(R.string.estado_pendiente)
            EstadoEvento.REALIZADO -> getString(R.string.estado_realizado)
            EstadoEvento.APLAZADO -> getString(R.string.estado_aplazado)
        }
        actvStatus.setText(estadoStr, false)
        
        // Establecer ubicación
        evento.ubicacion?.let {
            selectedUbicacion = it
            etLocation.setText(it.direccion)
        }
        
        // Establecer contacto
        evento.contacto?.let {
            selectedContacto = it
            etContact.setText(it.nombre)
        }
        
        // Establecer recordatorio
        val recordatorioStr = when (evento.recordatorio) {
            TipoRecordatorio.SIN_RECORDATORIO -> getString(R.string.opciones_recordatorio_sin)
            TipoRecordatorio.HORA_EVENTO -> getString(R.string.opciones_recordatorio_hora)
            TipoRecordatorio.DIEZ_MINUTOS_ANTES -> getString(R.string.opciones_recordatorio_diez_min)
            TipoRecordatorio.UN_DIA_ANTES -> getString(R.string.opciones_recordatorio_dia)
        }
        actvReminder.setText(recordatorioStr, false)
    }
    
    private fun guardarEvento() {
        // Validar campos
        val descripcion = etDescription.text.toString().trim()
        
        if (descripcion.isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.completar_campos_requeridos,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Obtener estado seleccionado
        val estadoStr = actvStatus.text.toString()
        val estado = when {
            estadoStr == getString(R.string.estado_pendiente) -> EstadoEvento.PENDIENTE
            estadoStr == getString(R.string.estado_realizado) -> EstadoEvento.REALIZADO
            estadoStr == getString(R.string.estado_aplazado) -> EstadoEvento.APLAZADO
            else -> EstadoEvento.PENDIENTE
        }
        
        // Obtener recordatorio seleccionado
        val recordatorioStr = actvReminder.text.toString()
        val recordatorio = when (recordatorioStr) {
            getString(R.string.opciones_recordatorio_sin) -> TipoRecordatorio.SIN_RECORDATORIO
            getString(R.string.opciones_recordatorio_hora) -> TipoRecordatorio.HORA_EVENTO
            getString(R.string.opciones_recordatorio_diez_min) -> TipoRecordatorio.DIEZ_MINUTOS_ANTES
            getString(R.string.opciones_recordatorio_dia) -> TipoRecordatorio.UN_DIA_ANTES
            else -> TipoRecordatorio.SIN_RECORDATORIO
        }
        
        // Crear el evento actualizado
        val eventoActualizado = Evento(
            id = evento.id,
            categoria = selectedCategory,
            descripcion = descripcion,
            fecha = selectedDate,
            hora = selectedTime,
            estado = estado,
            ubicacion = selectedUbicacion,
            contacto = selectedContacto,
            recordatorio = recordatorio
        )
        
        // Guardar en la base de datos
        val filasActualizadas = eventoRepository.actualizarEvento(eventoActualizado)
        
        if (filasActualizadas) {
            // Programar o cancelar recordatorio según sea necesario
            if (recordatorio != TipoRecordatorio.SIN_RECORDATORIO) {
                RecordatorioUtil.programarRecordatorio(requireContext(), eventoActualizado)
            } else {
                RecordatorioUtil.cancelarRecordatorio(requireContext(), eventoActualizado.id)
            }
            
            Toast.makeText(
                requireContext(),
                R.string.evento_actualizado_exitosamente,
                Toast.LENGTH_SHORT
            ).show()
            
            // Notificar al fragmento padre
            listener?.onEventoEditado(eventoActualizado)
            
            // Volver al fragmento anterior
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.error_actualizando_evento,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun confirmarEliminarEvento() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmar_eliminar_evento)
            .setMessage(R.string.pregunta_confirmar_eliminar_evento)
            .setPositiveButton(R.string.si) { _, _ -> eliminarEvento() }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun eliminarEvento() {
        val eliminado = eventoRepository.eliminarEvento(evento.id)
        
        if (eliminado) {
            // Cancelar cualquier recordatorio asociado
            RecordatorioUtil.cancelarRecordatorio(requireContext(), evento.id)
            
            Toast.makeText(
                requireContext(),
                R.string.evento_eliminado_exitosamente,
                Toast.LENGTH_SHORT
            ).show()
            
            // Notificar al fragmento padre
            listener?.onEventoEliminado(evento.id)
            
            // Volver al fragmento anterior
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.error_eliminando_evento,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}