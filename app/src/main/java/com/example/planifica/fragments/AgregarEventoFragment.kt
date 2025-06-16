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
 * Fragmento para la creación de nuevos eventos.
 */
class AgregarEventoFragment : Fragment() {
    
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
    
    private var selectedCategory: TipoEvento = TipoEvento.CITA
    private var selectedDate: Date = Calendar.getInstance().time
    private var selectedTime: String = ""
    private var selectedContacto: Contacto? = null
    private var selectedUbicacion: Ubicacion? = null
    
    private val eventoRepository by lazy { EventoRepository(requireContext()) }
    
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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_agregar_evento, container, false)
        
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
        
        // Configurar botón de guardar
        btnSaveEvent.setOnClickListener { guardarEvento() }
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
        
        // Selección inicial
        actualizarSeleccion(btnCategoryCita, TipoEvento.CITA)
    }
    
    private fun configurarCamposFechaHora() {
        // Configurar campo de fecha
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        etDate.setText(dateFormat.format(calendar.time))
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
        etTime.setText(timeFormat.format(calendar.time))
        selectedTime = timeFormat.format(calendar.time)
        
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
        actvStatus.setText(estadosAdapter.getItem(0), false)
        
        // Configurar dropdown de recordatorio
        val recordatoriosAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.opciones_recordatorio)
        )
        actvReminder.setAdapter(recordatoriosAdapter)
        actvReminder.setText(recordatoriosAdapter.getItem(0), false)
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
                    "Se requieren permisos de contactos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
        
        // Crear el evento
        val evento = Evento(
            id = 0, // Nuevo evento, el ID será asignado por la base de datos
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
        val id = eventoRepository.guardarEvento(evento)
        
        if (id > 0) {
            // Programar recordatorio si es necesario
            if (recordatorio != TipoRecordatorio.SIN_RECORDATORIO) {
                val eventoConId = evento.copy(id = id)
                RecordatorioUtil.programarRecordatorio(requireContext(), eventoConId)
            }
            
            Toast.makeText(
                requireContext(),
                R.string.evento_guardado_exitosamente,
                Toast.LENGTH_SHORT
            ).show()
            
            // Limpiar formulario o volver al fragmento anterior
            limpiarFormulario()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.error_guardando_evento,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun limpiarFormulario() {
        etDescription.text?.clear()
        selectedUbicacion = null
        selectedContacto = null
        etLocation.text?.clear()
        etContact.text?.clear()
        
        // Restablecer valores por defecto
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        etDate.setText(dateFormat.format(calendar.time))
        etTime.setText(timeFormat.format(calendar.time))
        selectedDate = calendar.time
        selectedTime = timeFormat.format(calendar.time)
        
        // Restablecer categoría por defecto
        configurarBotonesCategorias()
        
        // Restablecer dropdowns
        val estadosAdapter = actvStatus.adapter as ArrayAdapter<*>
        actvStatus.setText(estadosAdapter.getItem(0).toString(), false)
        
        val recordatoriosAdapter = actvReminder.adapter as ArrayAdapter<*>
        actvReminder.setText(recordatoriosAdapter.getItem(0).toString(), false)
    }
}
