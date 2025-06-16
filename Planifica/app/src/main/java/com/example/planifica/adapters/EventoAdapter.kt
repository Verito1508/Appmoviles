package com.example.planifica.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.planifica.R
import com.example.planifica.model.Evento
import com.example.planifica.model.EstadoEvento
import com.example.planifica.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar eventos en un RecyclerView.
 */
class EventoAdapter(
    private var eventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    /**
     * ViewHolder para un evento.
     */    
    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloElementoEvento)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHoraElementoEvento)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaElementoEvento)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoElementoEvento)
        val tvContacto: TextView = itemView.findViewById(R.id.tvContactoElementoEvento)
        val tvUbicacion: TextView = itemView.findViewById(R.id.tvUbicacionElementoEvento)
        val layoutContacto: View = itemView.findViewById(R.id.layoutContactoElementoEvento)
        val layoutUbicacion: View = itemView.findViewById(R.id.layoutUbicacionElementoEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(itemView)
    }    
    
    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        val context = holder.itemView.context
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Configurar título (descripción del evento)
        holder.tvTitulo.text = evento.descripcion
        
        // Configurar fecha y hora en un solo campo
        holder.tvFechaHora.text = String.format("%s - %s", dateFormat.format(evento.fecha), evento.hora)
        
        // Configurar categoría
        val categoriaTexto = when (evento.categoria) {
            TipoEvento.CITA -> context.getString(R.string.categoria_cita)
            TipoEvento.JUNTA -> context.getString(R.string.categoria_junta)
            TipoEvento.ENTREGA_PROYECTO -> context.getString(R.string.categoria_entrega_proyecto)
            TipoEvento.EXAMEN -> context.getString(R.string.categoria_examen)
            TipoEvento.OTRO -> context.getString(R.string.categoria_otro)
        }
        holder.tvCategoria.text = categoriaTexto
        
        // Configurar estado
        val estadoTexto = when (evento.estado) {
            EstadoEvento.PENDIENTE -> context.getString(R.string.estado_pendiente)
            EstadoEvento.REALIZADO -> context.getString(R.string.estado_realizado)
            EstadoEvento.APLAZADO -> context.getString(R.string.estado_aplazado)
        }
        holder.tvEstado.text = estadoTexto
        
        // Configurar color según estado y background
        when (evento.estado) {
            EstadoEvento.PENDIENTE -> {
                holder.tvEstado.setBackgroundResource(R.drawable.insignia_estado_pendiente)
                holder.tvEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            EstadoEvento.REALIZADO -> {
                holder.tvEstado.setBackgroundResource(R.drawable.insignia_estado_listo)
                holder.tvEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            EstadoEvento.APLAZADO -> {
                holder.tvEstado.setBackgroundResource(R.drawable.insignia_estado_pospuesta)
                holder.tvEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
        }
        
        // Configurar contacto
        evento.contacto?.let {
            holder.layoutContacto.visibility = View.VISIBLE
            holder.tvContacto.text = it.nombre
        } ?: run {
            holder.layoutContacto.visibility = View.GONE
        }
        
        // Configurar ubicación
        evento.ubicacion?.let {
            holder.layoutUbicacion.visibility = View.VISIBLE
            holder.tvUbicacion.text = it.direccion
        } ?: run {
            holder.layoutUbicacion.visibility = View.GONE
        }
        
        // Configurar clic en el item
        holder.itemView.setOnClickListener { onItemClick(evento) }
    }

    override fun getItemCount(): Int = eventos.size

    /**
     * Actualiza los datos del adaptador.
     * @param nuevosEventos La nueva lista de eventos.
     */
    fun actualizarDatos(nuevosEventos: List<Evento>) {
        this.eventos = nuevosEventos
        notifyDataSetChanged()
    }

    /**
     * Filtra los eventos por tipo.
     * @param tipo El tipo de evento para filtrar, o null para mostrar todos.
     * @param listaCompleta La lista completa de eventos para filtrar.
     */
    fun filtrarPorTipo(tipo: TipoEvento?, listaCompleta: List<Evento>) {
        eventos = if (tipo == null) {
            listaCompleta
        } else {
            listaCompleta.filter { it.categoria == tipo }
        }
        notifyDataSetChanged()
    }
}