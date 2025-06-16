package com.example.planifica.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.planifica.MainActivity
import com.example.planifica.R

/**
 * Receptor de recordatorios para eventos.
 */
class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        private const val CHANNEL_ID = "evento_recordatorio_channel"
        private const val NOTIFICATION_ID = 1
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val eventoId = intent.getLongExtra("evento_id", 0)
        val eventoDescripcion = intent.getStringExtra("evento_descripcion") ?: ""
        val eventoCategoria = intent.getStringExtra("evento_categoria") ?: ""
        
        // Crear intent para abrir la aplicación al tocar la notificación
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("evento_id", eventoId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Crear canal de notificación para Android 8.0+
        createNotificationChannel(context)
        
        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(eventoCategoria)
            .setContentText(eventoDescripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(eventoId.toInt(), notificationBuilder.build())
    }
    
    /**
     * Crea un canal de notificación para Android 8.0+.
     * @param context El contexto de la aplicación.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios"
            val descriptionText = "Recordatorios de eventos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
