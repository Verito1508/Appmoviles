package com.example.planifica.repository

import android.content.Context
import com.example.planifica.database.EventoDBHelper
import com.example.planifica.model.Evento
import com.example.planifica.model.EstadoEvento
import java.util.Calendar
import java.util.Date

/**
 * Repositorio para acceder a los datos de eventos.
 */
class EventoRepository(context: Context) {
    
    private val dbHelper = EventoDBHelper(context)
    
    /**
     * Guarda un nuevo evento en la base de datos.
     * @param evento El evento a guardar.
     * @return El ID del evento guardado o -1 si ocurre un error.
     */
    fun guardarEvento(evento: Evento): Long {
        return dbHelper.insertarEvento(evento)
    }
    
    /**
     * Actualiza un evento existente.
     * @param evento El evento con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    fun actualizarEvento(evento: Evento): Boolean {
        return dbHelper.actualizarEvento(evento) > 0
    }
    
    /**
     * Elimina un evento.
     * @param id El ID del evento a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    fun eliminarEvento(id: Long): Boolean {
        return dbHelper.eliminarEvento(id) > 0
    }
    
    /**
     * Actualiza el estado de un evento.
     * @param id El ID del evento.
     * @param nuevoEstado El nuevo estado del evento.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    fun actualizarEstadoEvento(id: Long, nuevoEstado: EstadoEvento): Boolean {
        return dbHelper.actualizarEstadoEvento(id, nuevoEstado) > 0
    }
    
    /**
     * Obtiene todos los eventos para el día actual.
     * @return Lista de eventos para el día actual.
     */
    fun obtenerEventosHoy(): List<Evento> {
        val hoy = Calendar.getInstance().time
        return dbHelper.obtenerEventosPorFecha(hoy)
    }
    
    /**
     * Obtiene todos los eventos para una fecha específica.
     * @param fecha La fecha para la cual se quieren obtener los eventos.
     * @return Lista de eventos para esa fecha.
     */
    fun obtenerEventosPorFecha(fecha: Date): List<Evento> {
        return dbHelper.obtenerEventosPorFecha(fecha)
    }
    
    /**
     * Obtiene todos los eventos para un rango de fechas.
     * @param fechaInicial La fecha inicial del rango.
     * @param fechaFinal La fecha final del rango.
     * @return Lista de eventos para ese rango de fechas.
     */
    fun obtenerEventosPorRangoFechas(fechaInicial: Date, fechaFinal: Date): List<Evento> {
        return dbHelper.obtenerEventosPorRangoFechas(fechaInicial, fechaFinal)
    }
    
    /**
     * Obtiene todos los eventos para un mes específico.
     * @param mes El mes (1-12).
     * @param año El año.
     * @return Lista de eventos para ese mes.
     */
    fun obtenerEventosPorMes(mes: Int, año: Int): List<Evento> {
        return dbHelper.obtenerEventosPorMes(mes, año)
    }
    
    /**
     * Obtiene todos los eventos para un año específico.
     * @param año El año.
     * @return Lista de eventos para ese año.
     */
    fun obtenerEventosPorAño(año: Int): List<Evento> {
        return dbHelper.obtenerEventosPorAño(año)
    }
    
    /**
     * Obtiene todos los eventos futuros a partir de hoy.
     * @return Lista de eventos futuros.
     */
    fun obtenerEventosFuturos(): List<Evento> {
        val hoy = Calendar.getInstance().time
        val todosLosEventos = dbHelper.obtenerTodosLosEventos()
        return todosLosEventos.filter { it.fecha >= hoy }
    }
    
    /**
     * Obtiene todos los eventos.
     * @return Lista de todos los eventos.
     */
    fun obtenerTodosLosEventos(): List<Evento> {
        return dbHelper.obtenerTodosLosEventos()
    }
}
