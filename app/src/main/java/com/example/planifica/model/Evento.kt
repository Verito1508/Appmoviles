package com.example.planifica.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Modelo de datos para un evento en la aplicación.
 */
@Parcelize
data class Evento(
    val id: Long = 0,
    val categoria: TipoEvento,
    val descripcion: String,
    val fecha: Date,
    val hora: String,
    val estado: EstadoEvento,
    val ubicacion: Ubicacion?,
    val contacto: Contacto?,
    val recordatorio: TipoRecordatorio
) : Parcelable

/**
 * Enumeración para los diferentes tipos de eventos.
 */
enum class TipoEvento {
    CITA,
    JUNTA,
    ENTREGA_PROYECTO,
    EXAMEN,
    OTRO
}

/**
 * Enumeración para los diferentes estados de un evento.
 */
enum class EstadoEvento {
    PENDIENTE,
    REALIZADO,
    APLAZADO
}

/**
 * Enumeración para los diferentes tipos de recordatorio.
 */
enum class TipoRecordatorio {
    SIN_RECORDATORIO,
    HORA_EVENTO,
    DIEZ_MINUTOS_ANTES,
    UN_DIA_ANTES
}

/**
 * Modelo de datos para la ubicación de un evento.
 */
@Parcelize
data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val direccion: String
) : Parcelable

/**
 * Modelo de datos para el contacto asociado a un evento.
 */
@Parcelize
data class Contacto(
    val id: String,
    val nombre: String,
    val telefono: String? = null,
    val email: String? = null
) : Parcelable
