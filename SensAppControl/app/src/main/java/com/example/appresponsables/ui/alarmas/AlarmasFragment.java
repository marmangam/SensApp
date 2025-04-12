// Este archivo define el fragmento `AlarmasFragment`.
// Permite a los usuarios responsables ver todas las alarmas existentes (paginación) y borrarlas.
// Para esto se incluyen un método para obtener todas las alarmas
// y otro para eliminar la alarma seleccionada.
// También permite navegar al fragmento responsable de crear nuevas alarmas pulsando el botón correspondiente y
// navegar al fragmento responsable de editar alarmas ya creadas pulsando en la alarma deseada.


package com.example.appresponsables.ui.alarmas;

// Imports
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.appresponsables.Alarma;
import com.example.appresponsables.ListaAdaptadorAlarmas;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.appresponsables.databinding.FragmentAlarmasBinding;
import com.google.gson.Gson;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.time.LocalTime;
import java.util.Set;
import com.example.appresponsables.utilidades.MarginItemDecoration;
import com.google.gson.GsonBuilder;
import com.fatboyindustrial.gsonjavatime.Converters;




public class AlarmasFragment extends Fragment {
    // Variables de la clase
    private ListaAdaptadorAlarmas adaptador;
    private List<Alarma> alarmas;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    private NavController navController;
    private FragmentAlarmasBinding binding;
    Set<String> alarmas_nombre = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentAlarmasBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_responsables);
        // Obtenemos la actividad y los datos del Intent (dni_usuario del responsable que ha accedido a la aplicación)
        activity = getActivity();
        if (activity !=null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                dni_usuario = UsuarioObject.getString("dni_usuario");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Configuramos la vista para mostrar las alarmas en una matriz de 2 columnas
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_alarmas);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_alarma_margen);
        recyclerView.addItemDecoration(new MarginItemDecoration(margen));
        alarmas = new ArrayList<>();
        // Definimos un ListaAdaptadorAlarmas para mostrar la lista
        adaptador = new ListaAdaptadorAlarmas(alarmas, new ListaAdaptadorAlarmas.OnAlarmaClickListener() {
            @Override
            public void onAlarmaClick(Alarma alarma) {
                // Funcionamiento al hacer click en una alarma

                // Añadimos al bundle la alarma seleccionada y navegamos al fragmento AlarmasEditarFragment
                Bundle bundle = new Bundle();
                //Log.d("AlarmasFragment", "JSON de alarma: " + alarma.getHora().toString());
                Gson gson = Converters.registerLocalTime(new GsonBuilder()).create();
                String json = gson.toJson(alarma);
                bundle.putString("alarmaObject", json);
                Log.d("AlarmasFragment", "JSON de alarma: " + json);
               navController.navigate(R.id.action_nav_alarmas_to_nav_alarmas_editar, bundle);

            }

            @Override
            public void onAlarmaLongClick(Alarma alarma) {
                // Funcionamiento al hacer click largo en una alarma

                // Mostramos un mensaje de confirmación para asegurarnos que el usuario quiere eliminar la alarma
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                TextView titulo = new TextView(getContext());
                titulo.setText("Eliminar alarma");
                titulo.setTextSize(30);
                titulo.setPadding(25, 25, 25, 25);
                titulo.setTypeface(null, Typeface.BOLD);
                titulo.setGravity(Gravity.CENTER);
                builder.setCustomTitle(titulo);

                TextView mensaje = new TextView(getContext());
                mensaje.setText("¿Estás seguro que quieres eliminar la alarma "+ alarma.getNombre() + " ?");
                mensaje.setTextSize(20);
                mensaje.setPadding(25, 25, 25, 25);
                mensaje.setGravity(Gravity.CENTER);
                builder.setView(mensaje);

                AlertDialog.Builder builder1 = builder.setPositiveButton("Sí", (dialog, which) -> {
                            // En caso de que quiera eliminar la alarma, la borramos de la lista, notificamos al adaptador del cambio y ejecutamos el método eliminar_alarma(..)
                            alarmas.remove(alarma);
                            adaptador.notifyDataSetChanged();
                            eliminar_alarma(alarma.getDni_usuario(), alarma.getNombre());
                        })
                        .setNegativeButton("No", null);
                AlertDialog dialogo = builder.create();
                dialogo.show();

                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);
                }
        });

        // Seleccionamos el adaptador creado y mostramos todas las alarmas
        recyclerView.setAdapter(adaptador);

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewAlarmas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == alarmas.size() - 1) {
                        offset += max;
                        Log.d("AlarmasFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_alarmas(dni_usuario, offset, max);
                    }
                }
            }
        });

        // Mostramos las primeras alarmas
        mostrar_alarmas(dni_usuario,offset, max);

        // Funcionamiento del botón crear alarma
        Button boton_crear_alarma = view.findViewById(R.id.boton_crear_alarma);
        boton_crear_alarma.setOnClickListener(v ->{
            // Navegamos al Fragment AlarmasCrearFragment
            navController.navigate(R.id.action_nav_alarmas_to_nav_alarmas_crear);
        });
        return view;
    }

    // Método para obtener max  alarmas de un dni_usuario, enviando la solicitud al servidor
    public void mostrar_alarmas(String dni_usuario, int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("AlarmasFragment", "SE LLAMA A MOSTRAR ALARMAS, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "alarma/lista?dni_usuario=" + dni_usuario + "&offset=" + offset + "&limit=" + max;

            // Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("AlarmasFragment", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                // Añadimos todas las alarmas que no estén añadidas ya
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject alarma = response.getJSONObject(i);
                                    String nombre = alarma.getString("nombre");
                                    if (!alarmas_nombre.contains(nombre)) {
                                        alarmas_nombre.add(nombre);
                                        String dni_usuario = alarma.getString("dni_usuario");
                                        String tipo = alarma.getString("tipo");
                                        String s_hora = alarma.getString("hora");
                                        LocalTime hora = LocalTime.parse(s_hora);
                                        LocalDate completado = LocalDate.of(2020, 1, 1);
                                        if (!alarma.isNull("completado")) {
                                            String s_completado = alarma.getString("completado");
                                            completado = LocalDate.parse(s_completado);
                                        }
                                        alarmas.add(new Alarma(dni_usuario, nombre, tipo, hora, completado));
                                        add += 1;
                                    }
                                }
                                Log.d("AlarmasFragment", "Cantidad agregados nuevos: " + add);
                                // Se ordenan por horas y por si se han completado hoy o no
                               alarmas.sort((a1, a2) -> {
                                    LocalDate fecha_actua = LocalDate.now();

                                    // Comprobar si ambas alarmas se han completadas hoy
                                    boolean a1CompletadoHoy = a1.getCompletado() != null && a1.getCompletado().equals(fecha_actua);
                                    boolean a2CompletadoHoy = a2.getCompletado() != null && a2.getCompletado().equals(fecha_actua);

                                    // Si ambas alarmas se han completadas hoy, se ordenan por hora
                                    if (a1CompletadoHoy && a2CompletadoHoy) {
                                        return a1.getHora().compareTo(a2.getHora());
                                    }
                                    // Si solo una se ha completado hoy, se pone al final
                                    if (a1CompletadoHoy) {
                                        return 1;
                                    }
                                    if (a2CompletadoHoy) {
                                        return -1;
                                    }
                                    // Si ninguna se ha completado hoy, se ordenan por hora
                                    return a1.getHora().compareTo(a2.getHora());
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


    // Método para eliminar una alarma, enviando la solicitud al servidor
    public void eliminar_alarma(String dni_usuario, String nombre){
        final String URL = getString(R.string.IP) + "alarma?nombre=" + nombre + "&dni_usuario=" + dni_usuario;
        // Madamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Eliminado")) {
                                // Informamos al usuario de que la alarma ha sido eliminada correctamente
                                Toast.makeText(activity.getApplicationContext(), "Se ha eliminado la alarma correctamente", Toast.LENGTH_SHORT).show();
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
                        "Se ha producido un error eliminando la alarma", Toast.LENGTH_SHORT).show();
            }
        }) ;
        ServiciosWeb.getInstance().getRequestQueue().add(request);


    }

    // Método para asegurar que siempre se muestre la lista rellena
    @Override
    public void onResume() {
        super.onResume();
        alarmas.clear();
        alarmas_nombre.clear();
        offset = 0;
        final_lista = false;
        mostrar_alarmas(dni_usuario, offset, max);
    }


}
