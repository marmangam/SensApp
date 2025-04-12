// Este archivo define el fragmento `EventosFragment`.
// Permite a los usuarios responsables ver todos los eventos existentes (paginación) y borrarlos.
// Para esto se incluyen un método para obtener todos los eventos
// y otro para eliminar el evento seleccionado.
// También permite navegar al fragmento responsable de crear nuevos eventos pulsando el botón correspondiente y
// navegar al fragmento responsable de editar eventos ya creados pulsando en el evento deseado.

package com.example.appresponsables.ui.eventos;

// Imports
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appresponsables.Evento;
import com.example.appresponsables.ListaAdaptadorEventos;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentEventosBinding;
import com.example.appresponsables.utilidades.MarginItemDecoration;
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


public class EventosFragment extends Fragment {
    // Variables de la clase
    private ListaAdaptadorEventos adaptador;
    private List<Evento> eventos;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    private NavController navController;
    private FragmentEventosBinding binding;
    Set<String> eventos_nombre = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentEventosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_responsables);
        // Obtenemos la actividad y los datos del Intent (dni_usuario del responsable que ha accedido a la aplicación)
        activity = getActivity();
        if (activity != null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                dni_usuario = UsuarioObject.getString("dni_usuario");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Configuramos la vista para mostrar los eventos en una matriz de 1 columna
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_eventos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_evento_margen);
        recyclerView.addItemDecoration(new MarginItemDecoration(margen));
        eventos = new ArrayList<>();
        // Definimos un ListaAdaptadorEventos para mostrar la lista
        adaptador = new ListaAdaptadorEventos(eventos, new ListaAdaptadorEventos.OnEventoClickListener() {
            @Override
            public void onEventoClick(Evento evento) {
                // Funcionamiento al hacer click en un evento:
                // Añadimos al bundle el evento seleccionado y navegamos al fragmento EventosEditarFragment
                Bundle bundle = new Bundle();
                //Log.d("EventosFragment", "JSON de evento: " + evento.getDate().toString());
                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                String json = gson.toJson(evento);
                bundle.putString("eventoObject", json);
                //bundle.putString("hora", evento.getHora().toString());
                Log.d("EventosFragment", "JSON de evento: " + json);
                navController.navigate(R.id.action_nav_eventos_to_nav_eventos_editar, bundle);

            }

            @Override
            public void onEventoLongClick(Evento evento) {
                // Funcionamiento al hacer click largo en un evento:
                // Mostramos un mensaje de confirmación para asegurarnos que el usuario quiere eliminar el evento
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                TextView titulo = new TextView(getContext());
                titulo.setText("Eliminar evento");
                titulo.setTextSize(30);
                titulo.setPadding(25, 25, 25, 25);
                titulo.setTypeface(null, Typeface.BOLD);
                titulo.setGravity(Gravity.CENTER);
                builder.setCustomTitle(titulo);

                TextView mensaje = new TextView(getContext());
                mensaje.setText("¿Estás seguro que quieres eliminar el evento " + evento.getNombre() + " ?");
                mensaje.setTextSize(20);
                mensaje.setPadding(25, 25, 25, 25);
                mensaje.setGravity(Gravity.CENTER);
                builder.setView(mensaje);


                builder.setPositiveButton("Sí", (dialog, which) -> {
                            // En caso de que quiera eliminar el evento, lo borramos de la lista, notificamos al adaptador del cambio y ejecutamos el método eliminar_evento(..)
                            eventos.remove(evento);
                            adaptador.notifyDataSetChanged();
                            eliminar_evento(evento.getDni_usuario(), evento.getNombre());
                        })
                        .setNegativeButton("No", null);
                AlertDialog dialogo = builder.create();
                dialogo.show();

                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);

            }
        });

        // Seleccionamos el adaptador creado y mostramos todos los eventos
        recyclerView.setAdapter(adaptador);

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewEventos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == eventos.size() - 1) {
                        offset += max;
                        Log.d("EventosFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_eventos(dni_usuario, offset, max);
                    }
                }
            }
        });

        // Mostramos los primeros eventos
        mostrar_eventos(dni_usuario,offset, max);

        // Funcionamiento del botón crear evento
        Button boton_crear_evento = view.findViewById(R.id.boton_crear_evento);
        boton_crear_evento.setOnClickListener(v -> {
            // Navegamos al fragmento EventosCrearFragmet
            navController.navigate(R.id.action_nav_eventos_to_nav_eventos_crear);
        });
        return view;
    }


    // Método para obtener max eventos de un dni_usuario, enviando la solicitud al servidor
    public void mostrar_eventos(String dni_usuario, int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("EventosFragment", "SE LLAMA A MOSTRAR EVENTOS, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "evento/lista?dni_usuario=" + dni_usuario + "&offset=" + offset + "&limit=" + max;

            // Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("EventosFragment", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                // Añadimos todos los eventos que no estén añadidos ya
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject evento = response.getJSONObject(i);
                                    String nombre = evento.getString("nombre");
                                    if (!eventos_nombre.contains(nombre)) {
                                        eventos_nombre.add(nombre);
                                        String dni_usuario = evento.getString("dni_usuario");
                                        String tipo = evento.getString("tipo");
                                        String s_dia = evento.getString("dia");
                                        LocalDate dia = LocalDate.parse(s_dia);
                                        Boolean completado = evento.getBoolean("completado");
                                        eventos.add(new Evento(dni_usuario, nombre, tipo, dia, completado));
                                        add += 1;
                                    }
                                }
                                Log.d("EventosFragment", "Cantidad agregados nuevos: " + add);
                                // Se ordenan por fecha
                                eventos.sort((a1, a2) -> {
                                    return a1.getDia().compareTo(a2.getDia());
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


    // Método para eliminar un evento, enviando la solicitud al servidor
    public void eliminar_evento(String dni_usuario, String nombre) {
        final String URL = getString(R.string.IP) + "evento?nombre=" + nombre + "&dni_usuario=" + dni_usuario;
        // Madamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Eliminado")) {
                                // Informamos al usuario de que el evento ha sido eliminado correctamente
                                Toast.makeText(activity.getApplicationContext(), "Se ha eliminado el evento correctamente", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(activity.getApplicationContext(),
                        "Se ha producido un error eliminando el evento", Toast.LENGTH_SHORT).show();
            }
        });
        ServiciosWeb.getInstance().getRequestQueue().add(request);


    }

    // Método para asegurar que siempre se muestre la lista rellena
    @Override
    public void onResume() {
        super.onResume();
        eventos.clear();
        eventos_nombre.clear();
        offset = 0;
        final_lista = false;
        mostrar_eventos(dni_usuario, offset, max);
        }



}