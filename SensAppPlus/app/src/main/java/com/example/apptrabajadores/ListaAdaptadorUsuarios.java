// Este archivo define el adaptador `ListaAdaptadorUsuarios`.
// Es un adaptador personalizado para mostrar una lista de usuarios en un RecyclerView.

package com.example.apptrabajadores;

// Imports
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class ListaAdaptadorUsuarios extends RecyclerView.Adapter<ListaAdaptadorUsuarios.UsuarioViewHolder> {
    // Variables de la clase
    private final List<Usuario> usuarios;
    private final OnUsuarioClickListener onUsuarioClickListener;

    // Interfaz de la clase para manejar el evento de Click
    public interface OnUsuarioClickListener{
        void onUsuarioClick(Usuario usuario);
    }

    // Constructor para inicializar el adaptador con la lista de usuarios y el listener
    public ListaAdaptadorUsuarios(List<Usuario> usuarios, OnUsuarioClickListener listener) {
        this.usuarios = usuarios;
        this.onUsuarioClickListener = listener;
    }

    // Método para crear nuevas instancias de UsuariosViewHolder
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    // Método para establecer los datos de un usuario en una vista en el ViewHolder
    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarios.get(position);
        holder.textNombre.setText(usuario.getNombre() + " "+ usuario.getApellidos());
        holder.textDni.setText(usuario.getDni());

        // Manejo del evento Click en el elemento de la lista
        holder.itemView.setOnClickListener(v -> onUsuarioClickListener.onUsuarioClick(usuario));

    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    // Clase interna ViewHolder para representar cada elemento de la lista
    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre;
        TextView textDni;

        // Constructor para inicializar los datos del usuario
        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textView_nombre_usuario);
            textDni = itemView.findViewById(R.id.textView_dni_usuario);

        }
    }

}



