// Este archivo define el fragmento `ConsultarUsuarioFragment`.
// Permite a los operadores ver una lista de los usuarios (paginacion).
// Para esto se incluyen un método para obtener todos los usuarios.
// También permite navegar al fragmento responsable de consultar los datos de un usuario concreto pulsando en dicho usuario.

package com.example.apptrabajadores.ui.consultarUsuario;

// Imports
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.apptrabajadores.ListaAdaptadorUsuarios;
import com.example.apptrabajadores.Usuario;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentConsultarUsuarioBinding;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsultarUsuarioFragment extends Fragment {
    // Variables de la clase
    private FragmentConsultarUsuarioBinding binding;
    private ListaAdaptadorUsuarios adaptador;
    private List<Usuario> usuarios;
    private List<Usuario> usuarios_filtrados;
    private NavController navController;
    Set<String> usuarios_dni = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentConsultarUsuarioBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_operadores);


        // Configuramos la vista para mostrar los usuarios en una matriz de 1 columna
        usuarios = new ArrayList<>();
        usuarios_filtrados = new ArrayList<>();
        binding.recyclerViewUsuarios.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        binding.recyclerViewUsuarios.addItemDecoration(new MarginItemDecoration(margen));

        // Definimos un ListaAdaptadorUsuarios para mostrar la lista
        adaptador = new ListaAdaptadorUsuarios(usuarios_filtrados, new ListaAdaptadorUsuarios.OnUsuarioClickListener() {
            @Override
            public void onUsuarioClick(Usuario usuario) {
                // Funcionamiento al hacer click en un usuario:
                // Añadimos al bundle el usuario seleccionado y navegamos al fragmento UsuarioDetalleFragment
                Bundle bundle = new Bundle();
                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                String json = gson.toJson(usuario);
                bundle.putString("usuarioObject", json);
                Log.d("UsuariosFragment", "JSON del usuario: " + json);
                navController.navigate(R.id.action_nav_consultar_usuario_to_nav_detalle_usuario, bundle);

            }
        });
        // Seleccionamos el adaptador creado
        binding.recyclerViewUsuarios.setAdapter(adaptador);

        // Definimos una barra de búsqueda para poder filtrar la lista de todos los usuarios
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // Para filtrar la lista de usuarios ejecutamos filtrarLista, mandando como parámetro el dni escrito en la barra de búsqueda
            @Override
            public boolean onQueryTextChange(String dni) {
                filtrarLista(dni);
                return true;
            }
        });

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewUsuarios.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == usuarios_filtrados.size() - 1) {
                        offset += max;
                        Log.d("OperadoresFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_usuarios(offset, max);
                    }
                }
            }
        });

        // Mostramos los primeros usuarios
        mostrar_usuarios(offset, max);


        return view;
    }

    // Método para filtrar la lista de usuarios según un dni: se muestran solo los resultados de usuarios cuyos dni
    // contengan el dni pasado por parámetro
    private void filtrarLista(String dni) {
        usuarios_filtrados.clear();
        if (dni.isEmpty()) {
            usuarios_filtrados.addAll(usuarios);
        } else {
            for (Usuario usuario : usuarios) {
                // Se filtra por dni
                if (usuario.getDni().toLowerCase().contains(dni.toLowerCase())) {
                    usuarios_filtrados.add(usuario);
                }
            }
        }
        // Notificamos el cambio al adaptador
        adaptador.notifyDataSetChanged();
    }

    // Método para obtener todos los usuarios, enviando la solicitud al servidor
    public void mostrar_usuarios(int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("UsuariosFragment", "SE LLAMA A MOSTRAR USUARIOS, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
        final String URL = getString(R.string.IP) + "usuario/lista?offset=" + offset + "&limit=" + max;

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int add = 0;
                        Log.d("ConsultarUsuarioFragment", "Cantidad en response: " + response.length());
                        //Si llegamos al final de la lista lo indicamos
                        if (response.length() < max) {
                            final_lista = true;
                        }
                        try {
                            //Se añaden los usuarios que no estuvieran ya añadidos
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject usuario = response.getJSONObject(i);
                                String dni = usuario.getString("dni");
                                if (!usuarios_dni.contains(dni)) {
                                    usuarios_dni.add(dni);
                                String nombre = usuario.getString("nombre");
                                String apellidos = usuario.getString("apellidos");
                                String fecha = usuario.getString("fecha_nacimiento");
                                LocalDate f_nac = LocalDate.parse(fecha);
                                String domicilio = usuario.getString("domicilio");
                                String enf = usuario.getString("enfermedades_previas");
                                String alergias = usuario.getString("alergias");
                                Integer telefono = usuario.getInt("telefono");
                                String password = usuario.getString("contrasena");

                                usuarios.add(new Usuario(nombre, apellidos, f_nac, domicilio, enf, alergias, dni, telefono, password));
                                add += 1;
                            }
                            }
                            Log.d("ConsultarUsuarioFragment", "Cantidad agregados nuevos: " + add);
                            // Notificamos el cambio al adaptador
                            usuarios_filtrados.clear();
                            usuarios_filtrados.addAll(usuarios);
                            adaptador.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        cargando = false;
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
                cargando = false;
            }
        });

        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // Método para asegurar que siempre se muestre la lista rellena
    @Override
    public void onResume() {
        super.onResume();
        usuarios.clear();
        usuarios_dni.clear();
        offset = 0;
        final_lista = false;
        mostrar_usuarios(offset, max);

    }


}