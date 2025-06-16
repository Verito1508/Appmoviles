package com.example.planifica.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.planifica.model.Evento
import com.example.planifica.model.TipoRecordatorio
import com.example.planifica.receivers.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Clase utilitaria para programar recordatorios.
 */
object RecordatorioUtil {
    
    private const val FORMATO_HORA = "HH:mm"

    /**
     * Programa un recordatorio para un evento.
     * @param context El contexto de la aplicación.
     * @param evento El evento para el cual programar el recordatorio.
     */
    fun programarRecordatorio(context: Context, evento: Evento) {
        if (evento.recordatorio == TipoRecordatorio.SIN_RECORDATORIO) {
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("evento_id", evento.id)
            putExtra("evento_descripcion", evento.descripcion)
            putExtra("evento_categoria", evento.categoria.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            evento.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance()
        calendar.time = evento.fecha
        
        // Establecer la hora del evento
        val timeFormat = SimpleDateFormat(FORMATO_HORA, Locale.getDefault())
        val timeParts = evento.hora.split(":")
        if (timeParts.size == 2) {
            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
        }
        
        // Ajustar según el tipo de recordatorio
        when (evento.recordatorio) {
            TipoRecordatorio.HORA_EVENTO -> {
                // No ajustar, notificar a la hora exacta
            }
            TipoRecordatorio.DIEZ_MINUTOS_ANTES -> {
                calendar.add(Calendar.MINUTE, -10)
            }
            TipoRecordatorio.UN_DIA_ANTES -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            else -> {
                // Sin recordatorio, no programar
                return
            }
        }
        
        // Programar la alarma
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Cancela un recordatorio previamente programado para un evento.
     * @param context El contexto de la aplicación.
     * @param eventoId El ID del evento para el cual cancelar el recordatorio.
     */
    fun cancelarRecordatorio(context: Context, eventoId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventoId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}
