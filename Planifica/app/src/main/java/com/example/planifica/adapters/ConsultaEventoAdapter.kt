package com.example.planifica.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.planifica.R
import com.example.planifica.fragments.ConsultarEventosFragment
import com.example.planifica.model.Evento
import com.example.planifica.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar eventos en la pantalla de consulta.
 */
class ConsultaEventoAdapter(
    private var eventos: List<Evento>,
    private val onVerDetallesClick: (Evento) -> Unit,
    private val onEditarClick: (Evento) -> Unit,
    private val onEliminarClick: (Evento) -> Unit,
    private val onVerUbicacionClick: (Evento) -> Unit,
    private val onContactoClick: (Evento) -> Unit
) : RecyclerView.Adapter<ConsultaEventoAdapter.ConsultaEventoViewHolder>() {

    /**
     * ViewHolder para un evento en la consulta.
     */    
    class ConsultaEventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloConsultaElementoEvento)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHoraConsultaElementoEvento)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaConsultaElementoEvento)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoConsultaElementoEvento)
        val tvUbicacion: TextView = itemView.findViewById(R.id.tvUbicacionConsultaElementoEvento)
        val tvContacto: TextView = itemView.findViewById(R.id.tvContactoConsultaElementoEvento)
        val layoutContacto: View = itemView.findViewById(R.id.layoutContactoConsultaElementoEvento)
        val layoutUbicacion: View = itemView.findViewById(R.id.layoutUbicacionConsultaElementoEvento)
        val btnVerUbicacion: ImageButton = itemView.findViewById(R.id.btnVerUbicacion)
        val btnEditarEstado: View = itemView.findViewById(R.id.btnEditarEstado)
        val btnEliminarEvento: View = itemView.findViewById(R.id.btnEliminarEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaEventoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_consulta, parent, false)
        return ConsultaEventoViewHolder(itemView)
    }    override fun onBindViewHolder(holder: ConsultaEventoViewHolder, position: Int) {
        val evento = eventos[position]
        val context = holder.itemView.context
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Configurar título (descripción del evento)
        holder.tvTitulo.text = evento.descripcion
        
        // Al hacer clic en el título, se abre la pantalla de edición
        holder.tvTitulo.setOnClickListener { onEditarClick(evento) }
        
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
        holder.tvEstado.text = when (evento.estado) {
            com.example.planifica.model.EstadoEvento.PENDIENTE -> 
                context.getString(R.string.estado_pendiente)
            com.example.planifica.model.EstadoEvento.REALIZADO -> 
                context.getString(R.string.estado_realizado)
            com.example.planifica.model.EstadoEvento.APLAZADO -> 
                context.getString(R.string.estado_aplazado)
        }
        
        // Configurar ubicación si existe
        if (evento.ubicacion != null) {
            holder.layoutUbicacion.visibility = View.VISIBLE
            holder.tvUbicacion.text = evento.ubicacion.direccion
            holder.btnVerUbicacion.setOnClickListener { onVerUbicacionClick(evento) }
        } else {
            holder.layoutUbicacion.visibility = View.GONE
        }
        
        // Configurar contacto si existe
        if (evento.contacto != null) {
            holder.layoutContacto.visibility = View.VISIBLE
            holder.tvContacto.text = evento.contacto.nombre
            holder.layoutContacto.setOnClickListener { onContactoClick(evento) }
        } else {
            holder.layoutContacto.visibility = View.GONE
        }

        // Configurar botones de acción
        holder.btnEditarEstado.setOnClickListener { 
            // Crear una acción personalizada para actualizar solo el estado
            (context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager?.let { fragmentManager ->
                val fragment = fragmentManager.findFragmentById(R.id.fragmento_contenido_principal)
                if (fragment is ConsultarEventosFragment) {
                    fragment.mostrarDialogoActualizarEstado(evento)
                }
            }
        }
        holder.btnEliminarEvento.setOnClickListener { onEliminarClick(evento) }
    }

    override fun getItemCount(): Int = eventos.size

    /**
     * Actualiza los datos del adaptador.
     * @param nuevosEventos La nueva lista de eventos.
     */
    fun actualizarDatos(nuevosEventos: List<Evento>) {
        eventos = nuevosEventos
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
