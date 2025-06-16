package com.example.planifica.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.planifica.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase para manejar la base de datos SQLite de la aplicación.
 */
class EventoDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "eventos.db"
        private const val DATABASE_VERSION = 1

        // Tabla Eventos
        private const val TABLA_EVENTOS = "eventos"
        private const val COLUMNA_ID = "id"
        private const val COLUMNA_CATEGORIA = "categoria"
        private const val COLUMNA_DESCRIPCION = "descripcion"
        private const val COLUMNA_FECHA = "fecha"
        private const val COLUMNA_HORA = "hora"
        private const val COLUMNA_ESTADO = "estado"
        private const val COLUMNA_RECORDATORIO = "recordatorio"

        // Tabla Ubicaciones
        private const val TABLA_UBICACIONES = "ubicaciones"
        private const val COLUMNA_EVENTO_ID = "evento_id"
        private const val COLUMNA_LATITUD = "latitud"
        private const val COLUMNA_LONGITUD = "longitud"
        private const val COLUMNA_DIRECCION = "direccion"

        // Tabla Contactos
        private const val TABLA_CONTACTOS = "contactos"
        private const val COLUMNA_CONTACTO_ID = "contacto_id"
        private const val COLUMNA_NOMBRE = "nombre"
        private const val COLUMNA_TELEFONO = "telefono"
        private const val COLUMNA_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de eventos
        val crearTablaEventos = """
            CREATE TABLE $TABLA_EVENTOS (
                $COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMNA_CATEGORIA TEXT NOT NULL,
                $COLUMNA_DESCRIPCION TEXT,
                $COLUMNA_FECHA TEXT NOT NULL,
                $COLUMNA_HORA TEXT NOT NULL,
                $COLUMNA_ESTADO TEXT NOT NULL,
                $COLUMNA_RECORDATORIO TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(crearTablaEventos)

        // Crear tabla de ubicaciones
        val crearTablaUbicaciones = """
            CREATE TABLE $TABLA_UBICACIONES (
                $COLUMNA_EVENTO_ID INTEGER,
                $COLUMNA_LATITUD REAL,
                $COLUMNA_LONGITUD REAL,
                $COLUMNA_DIRECCION TEXT,
                PRIMARY KEY ($COLUMNA_EVENTO_ID),
                FOREIGN KEY ($COLUMNA_EVENTO_ID) REFERENCES $TABLA_EVENTOS($COLUMNA_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(crearTablaUbicaciones)

        // Crear tabla de contactos
        val crearTablaContactos = """
            CREATE TABLE $TABLA_CONTACTOS (
                $COLUMNA_EVENTO_ID INTEGER,
                $COLUMNA_CONTACTO_ID TEXT,
                $COLUMNA_NOMBRE TEXT,
                $COLUMNA_TELEFONO TEXT,
                $COLUMNA_EMAIL TEXT,
                PRIMARY KEY ($COLUMNA_EVENTO_ID),
                FOREIGN KEY ($COLUMNA_EVENTO_ID) REFERENCES $TABLA_EVENTOS($COLUMNA_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(crearTablaContactos)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización de base de datos, se eliminarán las tablas y se crearán de nuevo
        db.execSQL("DROP TABLE IF EXISTS $TABLA_CONTACTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_UBICACIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_EVENTOS")
        onCreate(db)
    }

    /**
     * Inserta un nuevo evento en la base de datos.
     * @param evento El evento a insertar.
     * @return El ID del evento insertado o -1 si ocurre un error.
     */
    fun insertarEvento(evento: Evento): Long {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val valuesEvento = ContentValues().apply {
            put(COLUMNA_CATEGORIA, evento.categoria.name)
            put(COLUMNA_DESCRIPCION, evento.descripcion)
            put(COLUMNA_FECHA, dateFormat.format(evento.fecha))
            put(COLUMNA_HORA, evento.hora)
            put(COLUMNA_ESTADO, evento.estado.name)
            put(COLUMNA_RECORDATORIO, evento.recordatorio.name)
        }

        val id = db.insert(TABLA_EVENTOS, null, valuesEvento)
        
        if (id != -1L) {
            // Si el evento tiene ubicación, guardarla
            evento.ubicacion?.let { ubicacion ->
                val valuesUbicacion = ContentValues().apply {
                    put(COLUMNA_EVENTO_ID, id)
                    put(COLUMNA_LATITUD, ubicacion.latitud)
                    put(COLUMNA_LONGITUD, ubicacion.longitud)
                    put(COLUMNA_DIRECCION, ubicacion.direccion)
                }
                db.insert(TABLA_UBICACIONES, null, valuesUbicacion)
            }
            
            // Si el evento tiene contacto, guardarlo
            evento.contacto?.let { contacto ->
                val valuesContacto = ContentValues().apply {
                    put(COLUMNA_EVENTO_ID, id)
                    put(COLUMNA_CONTACTO_ID, contacto.id)
                    put(COLUMNA_NOMBRE, contacto.nombre)
                    put(COLUMNA_TELEFONO, contacto.telefono)
                    put(COLUMNA_EMAIL, contacto.email)
                }
                db.insert(TABLA_CONTACTOS, null, valuesContacto)
            }
        }
        
        return id
    }

    /**
     * Actualiza un evento existente en la base de datos.
     * @param evento El evento con los datos actualizados.
     * @return El número de filas afectadas.
     */
    fun actualizarEvento(evento: Evento): Int {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val valuesEvento = ContentValues().apply {
            put(COLUMNA_CATEGORIA, evento.categoria.name)
            put(COLUMNA_DESCRIPCION, evento.descripcion)
            put(COLUMNA_FECHA, dateFormat.format(evento.fecha))
            put(COLUMNA_HORA, evento.hora)
            put(COLUMNA_ESTADO, evento.estado.name)
            put(COLUMNA_RECORDATORIO, evento.recordatorio.name)
        }

        val result = db.update(
            TABLA_EVENTOS,
            valuesEvento,
            "$COLUMNA_ID = ?",
            arrayOf(evento.id.toString())
        )
        
        // Actualizar o insertar ubicación
        evento.ubicacion?.let { ubicacion ->
            val valuesUbicacion = ContentValues().apply {
                put(COLUMNA_EVENTO_ID, evento.id)
                put(COLUMNA_LATITUD, ubicacion.latitud)
                put(COLUMNA_LONGITUD, ubicacion.longitud)
                put(COLUMNA_DIRECCION, ubicacion.direccion)
            }
            
            val existeUbicacion = db.query(
                TABLA_UBICACIONES,
                null,
                "$COLUMNA_EVENTO_ID = ?",
                arrayOf(evento.id.toString()),
                null, null, null
            ).use { it.count > 0 }
            
            if (existeUbicacion) {
                db.update(
                    TABLA_UBICACIONES,
                    valuesUbicacion,
                    "$COLUMNA_EVENTO_ID = ?",
                    arrayOf(evento.id.toString())
                )
            } else {
                db.insert(TABLA_UBICACIONES, null, valuesUbicacion)
            }
        } ?: run {
            // Si el evento ya no tiene ubicación, eliminar la existente
            db.delete(
                TABLA_UBICACIONES,
                "$COLUMNA_EVENTO_ID = ?",
                arrayOf(evento.id.toString())
            )
        }
        
        // Actualizar o insertar contacto
        evento.contacto?.let { contacto ->
            val valuesContacto = ContentValues().apply {
                put(COLUMNA_EVENTO_ID, evento.id)
                put(COLUMNA_CONTACTO_ID, contacto.id)
                put(COLUMNA_NOMBRE, contacto.nombre)
                put(COLUMNA_TELEFONO, contacto.telefono)
                put(COLUMNA_EMAIL, contacto.email)
            }
            
            val existeContacto = db.query(
                TABLA_CONTACTOS,
                null,
                "$COLUMNA_EVENTO_ID = ?",
                arrayOf(evento.id.toString()),
                null, null, null
            ).use { it.count > 0 }
            
            if (existeContacto) {
                db.update(
                    TABLA_CONTACTOS,
                    valuesContacto,
                    "$COLUMNA_EVENTO_ID = ?",
                    arrayOf(evento.id.toString())
                )
            } else {
                db.insert(TABLA_CONTACTOS, null, valuesContacto)
            }
        } ?: run {
            // Si el evento ya no tiene contacto, eliminar el existente
            db.delete(
                TABLA_CONTACTOS,
                "$COLUMNA_EVENTO_ID = ?",
                arrayOf(evento.id.toString())
            )
        }
        
        return result
    }

    /**
     * Elimina un evento de la base de datos.
     * @param id El ID del evento a eliminar.
     * @return El número de filas afectadas.
     */
    fun eliminarEvento(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_EVENTOS, "$COLUMNA_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Actualiza el estado de un evento.
     * @param id El ID del evento.
     * @param nuevoEstado El nuevo estado del evento.
     * @return El número de filas afectadas.
     */
    fun actualizarEstadoEvento(id: Long, nuevoEstado: EstadoEvento): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMNA_ESTADO, nuevoEstado.name)
        }
        return db.update(TABLA_EVENTOS, values, "$COLUMNA_ID = ?", arrayOf(id.toString()))
    }

    /**
     * Obtiene todos los eventos para una fecha específica.
     * @param fecha La fecha para la cual se quieren obtener los eventos.
     * @return Lista de eventos para esa fecha.
     */
    fun obtenerEventosPorFecha(fecha: Date): List<Evento> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaStr = dateFormat.format(fecha)
        
        val db = this.readableDatabase
        val eventos = mutableListOf<Evento>()
        
        val cursor = db.query(
            TABLA_EVENTOS,
            null,
            "$COLUMNA_FECHA = ?",
            arrayOf(fechaStr),
            null, null, null
        )
        
        procesarCursorEventos(cursor, eventos)
        
        return eventos
    }

    /**
     * Obtiene todos los eventos para un rango de fechas.
     * @param fechaInicial La fecha inicial del rango.
     * @param fechaFinal La fecha final del rango.
     * @return Lista de eventos para ese rango de fechas.
     */
    fun obtenerEventosPorRangoFechas(fechaInicial: Date, fechaFinal: Date): List<Evento> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaInicialStr = dateFormat.format(fechaInicial)
        val fechaFinalStr = dateFormat.format(fechaFinal)
        
        val db = this.readableDatabase
        val eventos = mutableListOf<Evento>()
        
        val cursor = db.query(
            TABLA_EVENTOS,
            null,
            "$COLUMNA_FECHA BETWEEN ? AND ?",
            arrayOf(fechaInicialStr, fechaFinalStr),
            null, null, "$COLUMNA_FECHA ASC"
        )
        
        procesarCursorEventos(cursor, eventos)
        
        return eventos
    }

    /**
     * Obtiene todos los eventos para un mes específico.
     * @param mes El mes (1-12).
     * @param año El año.
     * @return Lista de eventos para ese mes.
     */
    fun obtenerEventosPorMes(mes: Int, año: Int): List<Evento> {
        val mesStr = String.format("%02d", mes)
        val db = this.readableDatabase
        val eventos = mutableListOf<Evento>()
        
        val cursor = db.query(
            TABLA_EVENTOS,
            null,
            "$COLUMNA_FECHA LIKE ?",
            arrayOf("$año-$mesStr-%"),
            null, null, "$COLUMNA_FECHA ASC"
        )
        
        procesarCursorEventos(cursor, eventos)
        
        return eventos
    }

    /**
     * Obtiene todos los eventos para un año específico.
     * @param año El año.
     * @return Lista de eventos para ese año.
     */
    fun obtenerEventosPorAño(año: Int): List<Evento> {
        val db = this.readableDatabase
        val eventos = mutableListOf<Evento>()
        
        val cursor = db.query(
            TABLA_EVENTOS,
            null,
            "$COLUMNA_FECHA LIKE ?",
            arrayOf("$año-%"),
            null, null, "$COLUMNA_FECHA ASC"
        )
        
        procesarCursorEventos(cursor, eventos)
        
        return eventos
    }

    /**
     * Obtiene todos los eventos.
     * @return Lista de todos los eventos.
     */
    fun obtenerTodosLosEventos(): List<Evento> {
        val db = this.readableDatabase
        val eventos = mutableListOf<Evento>()
        
        val cursor = db.query(
            TABLA_EVENTOS,
            null,
            null, null, null, null,
            "$COLUMNA_FECHA ASC, $COLUMNA_HORA ASC"
        )
        
        procesarCursorEventos(cursor, eventos)
        
        return eventos
    }

    /**
     * Procesa un cursor de consulta de eventos y carga los datos en la lista proporcionada.
     * @param cursor El cursor con los datos de la consulta.
     * @param eventos La lista donde se cargarán los eventos.
     */
    private fun procesarCursorEventos(cursor: Cursor, eventos: MutableList<Evento>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        cursor.use { c ->
            val idIndex = c.getColumnIndex(COLUMNA_ID)
            val categoriaIndex = c.getColumnIndex(COLUMNA_CATEGORIA)
            val descripcionIndex = c.getColumnIndex(COLUMNA_DESCRIPCION)
            val fechaIndex = c.getColumnIndex(COLUMNA_FECHA)
            val horaIndex = c.getColumnIndex(COLUMNA_HORA)
            val estadoIndex = c.getColumnIndex(COLUMNA_ESTADO)
            val recordatorioIndex = c.getColumnIndex(COLUMNA_RECORDATORIO)
            
            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val categoria = TipoEvento.valueOf(c.getString(categoriaIndex))
                val descripcion = c.getString(descripcionIndex)
                val fecha = dateFormat.parse(c.getString(fechaIndex)) ?: Date()
                val hora = c.getString(horaIndex)
                val estado = EstadoEvento.valueOf(c.getString(estadoIndex))
                val recordatorio = TipoRecordatorio.valueOf(c.getString(recordatorioIndex))
                
                // Obtener ubicación si existe
                val ubicacion = obtenerUbicacionPorEventoId(id)
                
                // Obtener contacto si existe
                val contacto = obtenerContactoPorEventoId(id)
                
                val evento = Evento(
                    id = id,
                    categoria = categoria,
                    descripcion = descripcion,
                    fecha = fecha,
                    hora = hora,
                    estado = estado,
                    ubicacion = ubicacion,
                    contacto = contacto,
                    recordatorio = recordatorio
                )
                
                eventos.add(evento)
            }
        }
    }

    /**
     * Obtiene la ubicación para un evento específico.
     * @param eventoId El ID del evento.
     * @return La ubicación del evento o null si no tiene.
     */
    private fun obtenerUbicacionPorEventoId(eventoId: Long): Ubicacion? {
        val db = this.readableDatabase
        
        val cursor = db.query(
            TABLA_UBICACIONES,
            null,
            "$COLUMNA_EVENTO_ID = ?",
            arrayOf(eventoId.toString()),
            null, null, null
        )
        
        return cursor.use { c ->
            if (c.moveToFirst()) {
                val latitudIndex = c.getColumnIndex(COLUMNA_LATITUD)
                val longitudIndex = c.getColumnIndex(COLUMNA_LONGITUD)
                val direccionIndex = c.getColumnIndex(COLUMNA_DIRECCION)
                
                Ubicacion(
                    latitud = c.getDouble(latitudIndex),
                    longitud = c.getDouble(longitudIndex),
                    direccion = c.getString(direccionIndex)
                )
            } else {
                null
            }
        }
    }

    /**
     * Obtiene el contacto para un evento específico.
     * @param eventoId El ID del evento.
     * @return El contacto del evento o null si no tiene.
     */
    private fun obtenerContactoPorEventoId(eventoId: Long): Contacto? {
        val db = this.readableDatabase
        
        val cursor = db.query(
            TABLA_CONTACTOS,
            null,
            "$COLUMNA_EVENTO_ID = ?",
            arrayOf(eventoId.toString()),
            null, null, null
        )
        
        return cursor.use { c ->
            if (c.moveToFirst()) {
                val contactoIdIndex = c.getColumnIndex(COLUMNA_CONTACTO_ID)
                val nombreIndex = c.getColumnIndex(COLUMNA_NOMBRE)
                val telefonoIndex = c.getColumnIndex(COLUMNA_TELEFONO)
                val emailIndex = c.getColumnIndex(COLUMNA_EMAIL)
                
                Contacto(
                    id = c.getString(contactoIdIndex),
                    nombre = c.getString(nombreIndex),
                    telefono = c.getString(telefonoIndex),
                    email = c.getString(emailIndex)
                )
            } else {
                null
            }
        }
    }
}
