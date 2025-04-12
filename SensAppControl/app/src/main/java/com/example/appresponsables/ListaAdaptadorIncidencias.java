// Este archivo define el adaptador `ListaAdaptadorIncidencias`.
// Es un adaptador personalizado para mostrar una lista de incidencias en un RecyclerView.

package com.example.appresponsables;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class ListaAdaptadorIncidencias extends RecyclerView.Adapter<ListaAdaptadorIncidencias.IncidenciaViewHolder> {
    // Variables de la clase
    private final List<Incidencia> incidencias;
    private final OnIncidenciaClickListener onIncidenciaClickListener;

    // Interfaz de la clase para manejar el evento de Click
    public interface OnIncidenciaClickListener{
        void onIncidenciaClick(Incidencia incidencia);
    }

    // Constructor para inicializar el adaptador con la lista de incidencias y el listener
    public ListaAdaptadorIncidencias(List<Incidencia> incidencias, OnIncidenciaClickListener listener) {
        this.incidencias = incidencias;
        this.onIncidenciaClickListener = listener;
    }

    // Método para crear nuevas instancias de IncidenciaViewHolder
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.incidencia, parent, false);
        return new IncidenciaViewHolder(view);
    }

    // Método para establecer los datos de una incidencia en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia incidencia = incidencias.get(position);
        holder.textfecha.setText(incidencia.getFecha().toString());
        holder.texthora.setText(incidencia.getHora().toString());
        if(incidencia.getResuelta()){
            holder.itemView.setBackgroundResource(R.drawable.incidencia_resuelta);
        }else{
            holder.itemView.setBackgroundResource(R.drawable.incidencia_no_resuelta);
        }
        // Manejo del evento Click en el elemento de la lista
        holder.itemView.setOnClickListener(v -> onIncidenciaClickListener.onIncidenciaClick(incidencia));
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        TextView textfecha;
        TextView texthora;

        // Constructor para inicializar los datos de la incidencia
        public IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            textfecha = itemView.findViewById(R.id.textView_fecha_incidencia);
            texthora = itemView.findViewById(R.id.textView_hora_incidencia);

        }
    }

}



