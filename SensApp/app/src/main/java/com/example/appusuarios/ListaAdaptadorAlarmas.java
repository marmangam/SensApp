// Este archivo define el adaptador `ListaAdaptadorAlarmas`.
// Es un adaptador personalizado para mostrar una lista de alarmas en un RecyclerView.

package com.example.appusuarios;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class ListaAdaptadorAlarmas extends RecyclerView.Adapter<ListaAdaptadorAlarmas.AlarmaViewHolder> {
    // Variables de la clase
    private final List<Alarma> alarmas;

    private final OnAlarmaClickListener listener;

    // Interfaz de la clase para manejar el evento de LongClick
    public interface OnAlarmaClickListener{
        void onAlarmaLongClick(Alarma alarma);
    }

    // Constructor para inicializar el adaptador con la lista de alarmas y el listener
    public ListaAdaptadorAlarmas(List<Alarma> alarmas, OnAlarmaClickListener listener) {
        this.alarmas = alarmas;
        this.listener = listener;
    }


    // Método para crear nuevas instancias de AlarmaViewHolder
    public AlarmaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarma, parent, false);
        return new AlarmaViewHolder(view);
    }

    // Método para establecer los datos de una alarma en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull AlarmaViewHolder holder, int position) {
        Alarma alarma = alarmas.get(position);
        holder.textNombre.setText(alarma.getNombre());
        holder.textHora.setText(alarma.getHora().toString());
        holder.imagen.setImageResource(ObtenerIcono(alarma.getTipo()));
        LocalDate actual = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        if (alarma.getCompletado() != null && alarma.getCompletado().equals(actual)) {
            holder.itemView.setBackgroundResource(R.drawable.alarma_completa);
        } else if (alarma.getHora().isBefore(ahora)) {
            holder.itemView.setBackgroundResource(R.drawable.alarma_no_completa);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.alarma_no_es_hora);
        }

        // Manejo del evento LongClick en el elemento de la lista
        holder.itemView.setOnLongClickListener(v -> {
            listener.onAlarmaLongClick(alarma);
            return true;
        });


    }

    @Override
    public int getItemCount() {
        return alarmas.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class AlarmaViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre;
        TextView textHora;
        ImageView imagen;

        // Constructor para inicializar los datos de la alarma
        public AlarmaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textView_nombre_alarma);
            textHora = itemView.findViewById(R.id.textView_hora_alarma);
            imagen = itemView.findViewById(R.id.imagen_alarma);

        }
    }

    // Método para mostrar la imagen correcta dependiendo del tipo de alarma
    public int ObtenerIcono(String tipo){
        switch (tipo){
            case "Medicamento":
                return R.drawable.alarma_medicamento;
            case "Pomada":
                return R.drawable.alarma_pomada;
            case "Pastilla":
                return R.drawable.alarma_pastilla;
            case "Suplementos":
                return R.drawable.alarma_suplementos;
            case "Ejercicio":
                return R.drawable.alarma_ejercicio;
            case "Control Tensión":
                return R.drawable.alarma_control_tension;
            case "Control Glucosa":
                return R.drawable.alarma_control_glucosa;
            case "Cuidado personal":
                return R.drawable.alarma_cuidado_personal;
            case "Revisar Enchufes":
                return R.drawable.alarma_enchufe;
            case "Revisar Grifos":
                return R.drawable.alarma_grifos;
            case "Alimentar mascota":
                return R.drawable.alarma_alimentar_mascota;
            case "Otros":
                return R.drawable.alarma_otro;
            default:
                return R.drawable.alarma_otro;
        }
    }
}



