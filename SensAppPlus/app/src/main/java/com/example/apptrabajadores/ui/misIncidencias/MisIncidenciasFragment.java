// Este archivo define el fragmento `MisIncidenciasFragment`.
// Permite a los operadores ver todas sus incidencias atendidas (paginación)
// Para esto se incluyen un método para obtener todas las incidencias de un operador
// También permite navegar al fragmento responsable de consultar el detalle de una incidencia concreta
// pulsando en la incidencia deseada.

package com.example.apptrabajadores.ui.misIncidencias;

// Imports
import android.app.Activity;
import android.content.Intent;
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
import com.example.apptrabajadores.Incidencia;
import com.example.apptrabajadores.ListaAdaptadorIncidencias;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentMisIncidenciasBinding;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MisIncidenciasFragment extends Fragment {
    // Variables de la clase
    private FragmentMisIncidenciasBinding binding;

    private ListaAdaptadorIncidencias adaptador;
    private List<Incidencia> incidencias;
    private List<Incidencia> incidencias_filtradas;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String operador;
    private NavController navController;
    Set<Integer> incidencias_id = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentMisIncidenciasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_operadores);
        // Obtenemos la actividad y los datos del Intent (operador que ha accedido a la aplicación)
        activity = getActivity();
        if (activity != null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                operador = UsuarioObject.getString("usuario");
                Log.d("MisIncidenciasFragment", "JSON de evento: " + operador);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Configuramos la vista para mostrar las incidencias en una matriz de 1 columna
        incidencias = new ArrayList<>();
        incidencias_filtradas = new ArrayList<>();
        binding.recyclerViewIncidencias.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        binding.recyclerViewIncidencias.addItemDecoration(new MarginItemDecoration(margen));
        // Definimos un ListaAdaptadorIncidencias para mostrar la lista
        adaptador = new ListaAdaptadorIncidencias(incidencias_filtradas, new ListaAdaptadorIncidencias.OnIncidenciaClickListener() {
            @Override
            public void onIncidenciaClick(Incidencia incidencia) {
                // Funcionamiento al hacer click en una incidencia:
                // Añadimos al bundle la incidencia seleccionada y navegamos al fragmento MisIncidenciasDetalleFragment
                Bundle bundle = new Bundle();
                //Log.d("EventosFragment", "JSON de evento: " + evento.getDate().toString());
                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                String json = gson.toJson(incidencia);
                bundle.putString("incidenciaObject", json);
                bundle.putString("horastring", incidencia.getHora().toString());
                Log.d("IncidenciasFragment", "JSON de la incidencia: " + json);
                navController.navigate(R.id.action_nav_mis_incidencias_to_nav_detalle_mi_incidencias, bundle);

            }
        });
        // Seleccionamos el adaptador creado
        binding.recyclerViewIncidencias.setAdapter(adaptador);

        // Definimos una barra de búsqueda para poder filtrar la lista de todas las incidencias
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
        binding.recyclerViewIncidencias.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == incidencias_filtradas.size() - 1) {
                        offset += max;
                        Log.d("IncidenciasFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_incidencias(operador, offset, max);
                    }
                }
            }
        });

        // Mostramos las primeras incidencias
        mostrar_incidencias(operador, offset, max);


        return view;


    }


    // Método para filtrar la lista de incidencias según un dni: se muestran solo los resultados de incidencias cuyos dni
    // contengan el dni pasado por parámetro
    private void filtrarLista(String dni) {
        incidencias_filtradas.clear();
        if (dni.isEmpty()) {
            incidencias_filtradas.addAll(incidencias);
            incidencias_filtradas.sort((a1, a2) -> {
                if (a2.getFecha().equals(a1.getFecha())) {
                    return a2.getHora().compareTo(a1.getHora());
                } else {
                    return a2.getFecha().compareTo(a1.getFecha());
                }

            });
        } else {
            for (Incidencia incidencia : incidencias) {
                // Se filtra por dni
                if (incidencia.getDni_usuario().toLowerCase().contains(dni.toLowerCase())) {
                    incidencias_filtradas.add(incidencia);
                }
            }
        }
        // Notificamos el cambio al adaptador
        adaptador.notifyDataSetChanged();
    }

    // Método para obtener todas las incidencias, enviando la solicitud al servidor
    public void mostrar_incidencias(String operador, int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("MisIncidenciasFragment", "SE LLAMA A MOSTRAR INCIDENCIAS, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "incidencia/lista/" + operador + "?offset=" + offset + "&limit=" + max;

            // Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("MisIncidenciasFragment", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                //Se añaden las incidencias que no estuvieran ya añadidas
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject incidencia = response.getJSONObject(i);
                                    Integer id = incidencia.getInt("id");
                                    if (!incidencias_id.contains(id)) {
                                        incidencias_id.add(id);
                                        String dni_usuario = incidencia.getString("dni_usuario");
                                        String operador = incidencia.getString("operador");
                                        String descripcion = incidencia.getString("descripcion");
                                        String procedimiento = incidencia.getString("procedimiento");
                                        Boolean resuelta = incidencia.getBoolean("resuelta");
                                        String s_dia = incidencia.getString("fecha");
                                        LocalDate fecha = LocalDate.parse(s_dia);
                                        String s_hora = incidencia.getString("hora");
                                        LocalTime hora = LocalTime.parse(s_hora);

                                        incidencias.add(new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta, fecha, hora));
                                        add += 1;
                                    }
                                }
                                Log.d("MisIncidenciasFragment", "Cantidad agregados nuevos: " + add);
                                // Se ordenan por fecha
                                incidencias_filtradas.clear();
                                incidencias_filtradas.addAll(incidencias);
                                incidencias_filtradas.sort((a1, a2) -> {
                                    if (a2.getFecha().equals(a1.getFecha())) {
                                        return a2.getHora().compareTo(a1.getHora());
                                    } else {
                                        return a2.getFecha().compareTo(a1.getFecha());
                                    }

                                });
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

    // Método para asegurar que siempre se muestre la lista rellena
    @Override
    public void onResume() {
        super.onResume();
        incidencias.clear();
        incidencias_id.clear();
        offset = 0;
        final_lista = false;
        mostrar_incidencias(operador, offset, max);
    }

}