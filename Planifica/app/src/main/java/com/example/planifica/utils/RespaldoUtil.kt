package com.example.planifica.utils

import android.content.Context
import android.util.Log
import com.example.planifica.database.EventoDBHelper
import com.example.planifica.model.Evento
import com.example.planifica.repository.EventoRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase utilitaria para respaldo y restauración de datos.
 */
object RespaldoUtil {
    
    private const val TAG = "RespaldoUtil"
    private const val ARCHIVO_RESPALDO = "eventos_respaldo.json"
    
    /**
     * Respalda todos los eventos en un archivo JSON local.
     * @param context El contexto de la aplicación.
     * @return true si el respaldo fue exitoso, false en caso contrario.
     */
    fun respaldoLocal(context: Context): Boolean {
        try {
            val repository = EventoRepository(context)
            val eventos = repository.obtenerTodosLosEventos()
            
            val jsonArray = JSONArray()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            for (evento in eventos) {
                val jsonEvento = JSONObject().apply {
                    put("id", evento.id)
                    put("categoria", evento.categoria.name)
                    put("descripcion", evento.descripcion)
                    put("fecha", dateFormat.format(evento.fecha))
                    put("hora", evento.hora)
                    put("estado", evento.estado.name)
                    put("recordatorio", evento.recordatorio.name)
                    
                    // Agregar ubicación si existe
                    evento.ubicacion?.let { ubicacion ->
                        put("ubicacion", JSONObject().apply {
                            put("latitud", ubicacion.latitud)
                            put("longitud", ubicacion.longitud)
                            put("direccion", ubicacion.direccion)
                        })
                    }
                    
                    // Agregar contacto si existe
                    evento.contacto?.let { contacto ->
                        put("contacto", JSONObject().apply {
                            put("id", contacto.id)
                            put("nombre", contacto.nombre)
                            put("telefono", contacto.telefono ?: "")
                            put("email", contacto.email ?: "")
                        })
                    }
                }
                
                jsonArray.put(jsonEvento)
            }
            
            val jsonString = jsonArray.toString()
            
            // Guardar en archivo local
            val archivoRespaldo = File(context.filesDir, ARCHIVO_RESPALDO)
            FileOutputStream(archivoRespaldo).use { output ->
                output.write(jsonString.toByteArray())
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al realizar respaldo local: ${e.message}")
            return false
        }
    }
    
    /**
     * Restaura los eventos desde un archivo JSON local.
     * @param context El contexto de la aplicación.
     * @return true si la restauración fue exitosa, false en caso contrario.
     */
    fun restaurarLocal(context: Context): Boolean {
        try {
            val archivoRespaldo = File(context.filesDir, ARCHIVO_RESPALDO)
            if (!archivoRespaldo.exists()) {
                Log.e(TAG, "Archivo de respaldo no encontrado")
                return false
            }
            
            val jsonString = FileInputStream(archivoRespaldo).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            
            // Recrear la base de datos
            val dbHelper = EventoDBHelper(context)
            val db = dbHelper.writableDatabase
            
            // Limpiar las tablas existentes
            db.execSQL("DELETE FROM contactos")
            db.execSQL("DELETE FROM ubicaciones")
            db.execSQL("DELETE FROM eventos")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val repository = EventoRepository(context)
            
            for (i in 0 until jsonArray.length()) {
                val jsonEvento = jsonArray.getJSONObject(i)
                
                val categoria = com.example.planifica.model.TipoEvento.valueOf(jsonEvento.getString("categoria"))
                val descripcion = jsonEvento.getString("descripcion")
                val fecha = dateFormat.parse(jsonEvento.getString("fecha")) ?: Date()
                val hora = jsonEvento.getString("hora")
                val estado = com.example.planifica.model.EstadoEvento.valueOf(jsonEvento.getString("estado"))
                val recordatorio = com.example.planifica.model.TipoRecordatorio.valueOf(jsonEvento.getString("recordatorio"))
                
                // Obtener ubicación si existe
                val ubicacion = if (jsonEvento.has("ubicacion")) {
                    val jsonUbicacion = jsonEvento.getJSONObject("ubicacion")
                    com.example.planifica.model.Ubicacion(
                        latitud = jsonUbicacion.getDouble("latitud"),
                        longitud = jsonUbicacion.getDouble("longitud"),
                        direccion = jsonUbicacion.getString("direccion")
                    )
                } else null
                
                // Obtener contacto si existe
                val contacto = if (jsonEvento.has("contacto")) {
                    val jsonContacto = jsonEvento.getJSONObject("contacto")
                    com.example.planifica.model.Contacto(
                        id = jsonContacto.getString("id"),
                        nombre = jsonContacto.getString("nombre"),
                        telefono = if (jsonContacto.getString("telefono").isNotEmpty()) 
                            jsonContacto.getString("telefono") else null,
                        email = if (jsonContacto.getString("email").isNotEmpty()) 
                            jsonContacto.getString("email") else null
                    )
                } else null
                
                // Crear el evento
                val evento = com.example.planifica.model.Evento(
                    id = 0, // Nuevo ID asignado por la base de datos
                    categoria = categoria,
                    descripcion = descripcion,
                    fecha = fecha,
                    hora = hora,
                    estado = estado,
                    ubicacion = ubicacion,
                    contacto = contacto,
                    recordatorio = recordatorio
                )
                
                // Guardar el evento
                repository.guardarEvento(evento)
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar desde respaldo local: ${e.message}")
            return false
        }
    }
    
    /**
     * Prepara un archivo para respaldo en servicios externos.
     * @param context El contexto de la aplicación.
     * @return El archivo con los datos respaldados o null si ocurre un error.
     */
    fun prepararArchivoRespaldo(context: Context): File? {
        try {
            if (respaldoLocal(context)) {
                return File(context.filesDir, ARCHIVO_RESPALDO)
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error al preparar archivo de respaldo: ${e.message}")
            return null
        }
    }
}
