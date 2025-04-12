// Este archivo define el adaptador `ListaAdaptadorOperadores`.
// Es un adaptador personalizado para mostrar una lista de operadores en un RecyclerView.

package com.example.apptrabajadores;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListaAdaptadorOperadores extends RecyclerView.Adapter<ListaAdaptadorOperadores.OperadorViewHolder> {
    // Variables de la clase
    private final List<Operador> operadores;
    private OnOperadorClickListener onOperadorClickListener;

    // Interfaz de la clase para manejar el evento de Click
    public interface OnOperadorClickListener{
        void onOperadorClick(Operador operador);
    }

    // Constructor para inicializar el adaptador con la lista de operadores y el listener
    public ListaAdaptadorOperadores(List<Operador> operadores, OnOperadorClickListener listener) {
        this.operadores = operadores;
        this.onOperadorClickListener = listener;
    }

    // Método para crear nuevas instancias de OperadorViewHolder
    public OperadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operador, parent, false);
        return new OperadorViewHolder(view);
    }

    // Método para establecer los datos de un operador en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull OperadorViewHolder holder, int position) {
        Operador operador = operadores.get(position);
        holder.textUsuario.setText("@" +operador.getUsuario());
        holder.textNombre.setText(operador.getNombre() + " " + operador.getApellidos());

        if (operador.getOcupado() != null && operador.getOcupado().equals(Boolean.TRUE) && operador.getActivo().equals(Boolean.TRUE)) {
            holder.itemView.setBackgroundResource(R.drawable.operador_ocupado);
        } else if (operador.getOcupado().equals(Boolean.FALSE) && operador.getActivo().equals(Boolean.TRUE)) {
            holder.itemView.setBackgroundResource(R.drawable.operador_activo);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.operador_no_activo);
        }

        // Manejo del evento Click en el elemento de la lista
        holder.itemView.setOnClickListener(v -> onOperadorClickListener.onOperadorClick(operador));
    }

    @Override
    public int getItemCount() {
        return operadores.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class OperadorViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre;
        TextView textUsuario;

        // Constructor para inicializar los datos del operador
        public OperadorViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textView_nombre);
            textUsuario = itemView.findViewById(R.id.textView_usuario);

        }
    }


}



