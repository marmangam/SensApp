// Este archivo define el fragmento `OperadoresFragment`.
// Permite a los administrativos ver una lista de los operadores de un usuario.
// Para esto se incluyen un método para obtener todos los operadores.
// También permite navegar al fragmento responsable de editar los datos de un operador pulsando en dicho operador.
// y eliminar un operador haciendo long click en el y confirmandolo en un diálogo.

package com.example.apptrabajadores.ui.operadores;

// Imports
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.apptrabajadores.ListaAdaptadorOperadores;
import com.example.apptrabajadores.Operador;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentOperadoresBinding;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OperadoresFragment extends Fragment {
    // Variables de la clase
    private FragmentOperadoresBinding binding;
    private ListaAdaptadorOperadores adaptador;
    private List<Operador> operadores;
    private List<Operador> operadores_filtrados;
    Button boton_crear;
    private NavController navController;
    Set<String> operadores_usu = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentOperadoresBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_administrativos);

        // Configuramos la vista para mostrar los usuarios en una matriz de 1 columna
        operadores = new ArrayList<>();
        operadores_filtrados = new ArrayList<>();
        binding.recyclerViewOperadores.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        binding.recyclerViewOperadores.addItemDecoration(new MarginItemDecoration(margen));

        // Definimos un ListaAdaptadorOperadores para mostrar la lista
        adaptador = new ListaAdaptadorOperadores(operadores_filtrados, new ListaAdaptadorOperadores.OnOperadorClickListener() {
            @Override
            public void onOperadorClick(Operador operador) {
                // Funcionamiento al hacer click en un operador:
                // Añadimos al bundle el operador seleccionado y navegamos al fragmento OperadoresEditarFragment
                Bundle bundle = new Bundle();
                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                String json = gson.toJson(operador);
                bundle.putString("operadorObject", json);
                Log.d("OperadoresFragment", "JSON del usuario: " + json);
                navController.navigate(R.id.action_nav_operadores_to_nav_editar_operadores, bundle);

            }
        });
        // Seleccionamos el adaptador creado
        binding.recyclerViewOperadores.setAdapter(adaptador);

        // Definimos una barra de búsqueda para poder filtrar la lista de todos los operadores
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // Para filtrar la lista de operadores ejecutamos filtrarLista, mandando como parámetro el nombre de usuario escrito en la barra de búsqueda
            @Override
            public boolean onQueryTextChange(String usuario) {
                filtrarLista(usuario);
                return true;
            }
        });

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewOperadores.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == operadores_filtrados.size() - 1) {
                        offset += max;
                        Log.d("OperadoresFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_operadores(offset, max);
                    }
                }
            }
        });

        // Mostramos los primeros operadoress
        mostrar_operadores(offset, max);

        // Configuramos el botón crear operador
        boton_crear = view.findViewById(R.id.boton_crear_operador);
        boton_crear.setOnClickListener(v -> {
            // Navegamos al fragmento OperadoresCrearFragment
            navController.navigate(R.id.action_nav_operadores_to_nav_crear_operadores);
        });


        return view;
    }

    // Método para filtrar la lista de operadores según un nombre de usuario: se muestran solo los resultados de operadores cuyos username
    // contengan el username pasado por parámetro
    private void filtrarLista(String usuario) {
        operadores_filtrados.clear();
        if (usuario.isEmpty()) {
            operadores_filtrados.addAll(operadores);
            ordenar(operadores_filtrados);
        } else {
            for (Operador operador: operadores) {
                // Se filtra por  nombre de usuario
                if (operador.getUsuario().toLowerCase().contains(usuario.toLowerCase())) {
                    operadores_filtrados.add(operador);
                }
            }
            ordenar(operadores_filtrados);
        }
        // Notificamos el cambio al adaptador
        adaptador.notifyDataSetChanged();
    }

    // Método para obtener max operadores, enviando la solicitud al servidor
    public void mostrar_operadores(int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("OperadoresFragment", "SE LLAMA A MOSTRAR OPERADORES, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "operador/lista?offset=" + offset + "&limit=" + max;

            // Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("OperadoresFragment", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                //Se añaden los operadores que no estuvieran ya añadidos
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject operador = response.getJSONObject(i);
                                    String usuario = operador.getString("usuario");
                                    if (!operadores_usu.contains(usuario)) {
                                        operadores_usu.add(usuario);
                                        String nombre = operador.getString("nombre");
                                        String apellidos = operador.getString("apellidos");
                                        String password = operador.getString("contrasena");
                                        Boolean activo = operador.getBoolean("activo");
                                        Boolean ocupado = operador.getBoolean("ocupado");

                                        operadores.add(new Operador(usuario, nombre, apellidos, password, activo, ocupado));
                                        add += 1;
                                    }
                                }
                                Log.d("OperadoresFragment", "Cantidad agregados nuevos: " + add);
                                operadores_filtrados.clear();
                                operadores_filtrados.addAll(operadores);
                                //Los ordenamos
                                ordenar(operadores_filtrados);


                                // Notificamos el cambio al adaptador
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
        if (operadores.isEmpty()) {
            operadores.clear();
            operadores_usu.clear();
            operadores_filtrados.clear();
            offset = 0;
            final_lista = false;
            mostrar_operadores(offset, max);
        }
    }


    public static void ordenar(List<Operador> operadores) {
        // Se ordenan para mostrar primeros los activos y no ocupados, luego los activos y ocupados y finalmente los no activos
        operadores.sort((a1, a2) -> {
            boolean a1activo = a1.getActivo().equals(Boolean.TRUE);
            boolean a2activo = a2.getActivo().equals(Boolean.TRUE);
            boolean a1ocupado = a1.getOcupado().equals(Boolean.TRUE);
            boolean a2ocupado = a2.getOcupado().equals(Boolean.TRUE);

            if (a1activo && !a2activo) {
                return -1;
            }
            if (!a1activo && a2activo) {
                return 1;
            }
            if (a1activo && a2activo) {
                if (!a1ocupado && a2ocupado) {
                    return -1;
                }
                if (a1ocupado && !a2ocupado) {
                    return 1;
                }
            }
            if (!a1activo && !a2activo) {
                return 0;
            }

            return 0;
        });
    }

}