// Este archivo define el adaptador `ListaAdaptadorResponsables`.
// Es un adaptador personalizado para mostrar una lista de responsables en un RecyclerView.

package com.example.appresponsables;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class ListaAdaptadorResponsables extends RecyclerView.Adapter<ListaAdaptadorResponsables.ResponsableViewHolder> {
    // Variables de la clase
    private final List<Responsable> responsables;

    // Constructor para inicializar el adaptador con la lista de responsables y el listener
    public ListaAdaptadorResponsables(List<Responsable> responsables) {
        this.responsables = responsables;
    }

    // Método para crear nuevas instancias de ResponsableViewHolder
    public ResponsableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.responsable, parent, false);
        return new ResponsableViewHolder(view);
    }

    // Método para establecer los datos de un responsable en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ResponsableViewHolder holder, int position) {
        Responsable responsable = responsables.get(position);
        holder.textNombre.setText(responsable.getNombre() + " "+ responsable.getApellidos());
        holder.textTelefono.setText(String.valueOf(responsable.getTelefono()));
    }

    @Override
    public int getItemCount() {
        return responsables.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class ResponsableViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre;
        TextView textTelefono;

        // Constructor para inicializar los datos del responsable
        public ResponsableViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textView_nombre_responsable);
            textTelefono = itemView.findViewById(R.id.textView_telefono_responsable);

        }
    }

}



