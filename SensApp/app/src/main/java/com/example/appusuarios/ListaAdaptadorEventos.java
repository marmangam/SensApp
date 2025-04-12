// Este archivo define el adaptador `ListaAdaptadorEventos`.
// Es un adaptador personalizado para mostrar una lista de eventos en un RecyclerView.

package com.example.appusuarios;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class ListaAdaptadorEventos extends RecyclerView.Adapter<ListaAdaptadorEventos.EventoViewHolder> {
    // Variables de la clase
    private final List<Evento> eventos;
    private final OnEventoClickListener listener;

    // Interfaz de la clase para manejar el evento de LongClick
    public interface OnEventoClickListener{
        void onEventoLongClick(Evento evento);
    }
    // Constructor para inicializar el adaptador con la lista de eventos y el listener
    public ListaAdaptadorEventos(List<Evento> eventos, OnEventoClickListener listener) {
        this.eventos = eventos;
        this.listener = listener;
    }

    // Método para crear nuevas instancias de EventoViewHolder
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.evento, parent, false);
        return new EventoViewHolder(view);
    }

    // Método para establecer los datos de un evento en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventos.get(position);
        holder.textNombre.setText(evento.getNombre());
        holder.textDate.setText(evento.getDia().toString());
        holder.imagen.setImageResource(ObtenerIcono(evento.getTipo()));
        if (evento.getCompletado()){
            holder.itemView.setBackgroundResource(R.drawable.evento_completo);
        }else{
            holder.itemView.setBackgroundResource(R.drawable.evento_no_completo);
        }
        // Manejo del evento LongClick en el elemento de la lista
        holder.itemView.setOnLongClickListener(v -> {
            listener.onEventoLongClick(evento);
            return true;
        });


    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre;
        TextView textDate;
        ImageView imagen;

        // Constructor para inicializar los datos del evento
        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textView_nombre_evento);
            textDate = itemView.findViewById(R.id.textView_date_evento);
            imagen = itemView.findViewById(R.id.imagen_evento);

        }
    }

    // Método para mostrar la imagen correcta dependiendo del tipo de evento
    public int ObtenerIcono(String tipo){
        switch (tipo){
            case "Cita Médica":
                return R.drawable.evento_cita_medica;
            case "Reunión Familiar":
                return R.drawable.evento_reunion_familiar;
            case "Evento Deportivo":
                return R.drawable.evento_evento_desportivo;
            case "Celebración":
                return R.drawable.evento_celebracion;
            case "Cumpleaños":
                return R.drawable.evento_cumpleanos;
            case "Visita Domiciliaria":
                return R.drawable.evento_visita_domiciliaria;
            case "Tarea Doméstica":
                return R.drawable.evento_tarea_domestica;
            case "Evento Religioso":
                return R.drawable.evento_evento_religioso;
            case "Renovación Documentos":
                return R.drawable.evento_renovacion;
            case "Trámites":
                return R.drawable.evento_tramites;
            case "Visita":
                return R.drawable.evento_visita;
            case "Medicamento":
                return R.drawable.evento_medicamento;
            case "Actividad Física":
                return R.drawable.evento_actividad_fisica;
            case "Recado":
                return R.drawable.evento_recado;
            case "Entretenimiento":
                return R.drawable.evento_entretenimiento;
            case "Viaje":
                return R.drawable.evento_viaje;
            case "Reunión":
                return R.drawable.evento_reunion;
            case "Veterinario":
                return R.drawable.evento_veterinario;
            case "Compra":
                return R.drawable.evento_compra;
            case "Otro":
                return R.drawable.evento_otro;
            default:
                return R.drawable.evento_otro;
        }
    }
}



