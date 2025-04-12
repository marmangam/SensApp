// Este archivo define el fragmento `ResponsablesFragment`.
// Permite a los administrativos ver una lista de los responsables de un usuario (paginacion).
// Para esto se incluyen un método para obtener todos los responsables de un usuario.
// También permite navegar al fragmento responsable de editar los datos de un responsable pulsando en dicho usuario
// y eliminar un responsable haciendo long click en el y confirmandolo en un diálogo.

package com.example.apptrabajadores.ui.responsables;

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
import com.example.apptrabajadores.ListaAdaptadorResponsables;
import  com.example.apptrabajadores.R;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import com.example.apptrabajadores.Responsable;
import com.example.apptrabajadores.databinding.FragmentResponsablesBinding;
import com.example.apptrabajadores.ServiciosWeb;
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

public class ResponsablesFragment extends Fragment {
    // Variables de la clase
    private FragmentResponsablesBinding binding;
    private ListaAdaptadorResponsables adaptador;
    private List<Responsable> responsables;
    Activity activity;
    Intent intent;
    String dni_usuario;
    Button boton_crear;
    Bundle bundle;
    private NavController navController;
    Set<String> responsables_usu = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentResponsablesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_administrativos);
        // Obtenemos la actividad y los datos del bundle (dni del usuarios cuyos responsables queremos listar).
        activity = getActivity();
        bundle = getArguments();
        if (activity != null) {
            intent = activity.getIntent();
            if (bundle != null) {
                    dni_usuario= bundle.getString("dni_usuario");
                    Log.d("ResponsablesFragment", "JSON recibido: " + dni_usuario);
                  }
        }
        // Configuramos la vista para mostrar los responsables en una matriz de 1 columna
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_responsables);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        recyclerView.addItemDecoration(new MarginItemDecoration(margen));
        responsables = new ArrayList<>();
        // Definimos un ListaAdaptadorResponsables para mostrar la lista
        adaptador = new ListaAdaptadorResponsables(responsables, new ListaAdaptadorResponsables.OnResponsableClickListener() {
            @Override
            public void onResponsableClick(Responsable responsable) {
                // Funcionamiento al hacer click en un repsonsable:
                // Añadimos al bundle el responsable seleccionado y navegamos al fragmento ResponsablesEditarFragment
                Bundle bundle2 = new Bundle();
                Gson gson = Converters.registerAll(new GsonBuilder()).create();
                String json = gson.toJson(responsable);
                bundle2.putString("responsableObject", json);
                Log.d("ResponsableFragment", "JSON del responsable: " + json);
                navController.navigate(R.id.action_nav_responsables_to_nav_editar_responsables, bundle2);
            }

            @Override
            public void onResponsableLongClick(Responsable responsable) {
                // Funcionamiento al hacer long click en un responsable:
                // Creamos un diálogo para confirmar si se quiere eliminar al responsable, en caso de que si se elimina el responsable seleccionado.
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                TextView titulo = new TextView(getContext());
                titulo.setText("Eliminar responsable");
                titulo.setTextSize(30);
                titulo.setPadding(25, 25, 25, 25);
                titulo.setTypeface(null, Typeface.BOLD);
                titulo.setGravity(Gravity.CENTER);
                builder.setCustomTitle(titulo);

                TextView mensaje = new TextView(getContext());
                mensaje.setText("¿Estás seguro que quieres eliminar el responsable "+ responsable.getUsuario() + " ?");
                mensaje.setTextSize(20);
                mensaje.setPadding(25, 25, 25, 25);
                mensaje.setGravity(Gravity.CENTER);
                builder.setView(mensaje);

                builder.setPositiveButton("Sí", (dialog, which) ->  {
                            responsables.remove(responsable);
                            adaptador.notifyDataSetChanged();
                            eliminar_responsable(responsable.getUsuario());
                        })
                        .setNegativeButton("No", null);
                AlertDialog dialogo = builder.create();
                dialogo.show();

                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);
            }
        });
        // Seleccionamos el adaptador creado y mostramos todos los responsables
        recyclerView.setAdapter(adaptador);

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewResponsables.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == responsables.size() - 1) {
                        offset += max;
                        Log.d("ResponsablesFragment", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_responsables(dni_usuario, offset, max);
                    }
                }
            }
        });

        // Mostramos los primeros responsables
        mostrar_responsables(dni_usuario, offset, max);

        // Configuramos el botón crear responsable
        boton_crear = view.findViewById(R.id.boton_crear_responsable);
        boton_crear.setOnClickListener(v -> {
            // Navegamos al fragmento ResponsablesCrearFragment
            navController.navigate(R.id.action_nav_responsables_to_nav_crear_responsables, bundle);
        });



        return view;
    }


    // Método para obtener max responsables, enviando la solicitud al servidor
    public void mostrar_responsables(String dni_usuario, int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("ResponsablesFragment", "SE LLAMA A MOSTRAR RESPONSABLES, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "responsable/lista/" + dni_usuario + "?offset=" + offset + "&limit=" + max;
            Log.d("ResponsablesFragment", "URL de la solicitud: " + URL);

            //Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("ResponsablesFragment", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                // Añadimos todos los responsables que no estén añadidos ya
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject responsable = response.getJSONObject(i);
                                    String usuario = responsable.getString("usuario");
                                    if (!responsables_usu.contains(usuario)) {
                                        responsables_usu.add(usuario);
                                        String nombre = responsable.getString("nombre");
                                        String apellidos = responsable.getString("apellidos");
                                        String dni = responsable.getString("dni");
                                        String dni_usuario = responsable.getString("dni_usuario");
                                        Integer telefono = responsable.getInt("telefono");
                                        String password = responsable.getString("contrasena");
                                        responsables.add(new Responsable(usuario, nombre, apellidos, dni, dni_usuario, telefono, password));
                                        add += 1;
                                    }
                                }
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

    // Método para eliminar un responsable, enviando la solicitud al servidor
    public void eliminar_responsable(String usuario){
        final String URL = getString(R.string.IP) + "responsable/" + usuario;
        Log.d("ResponsablesFragment", "URL de la solicitud: " + URL);

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Eliminado")) {
                               // Toast.makeText(activity.getApplicationContext(), "Responsable eliminado correctamente", Toast.LENGTH_SHORT).show();
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
                        "Se ha producido un error eliminando el responsable", Toast.LENGTH_SHORT).show();
            }
        }) ;
        ServiciosWeb.getInstance().getRequestQueue().add(request);


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
        responsables.clear();
        responsables_usu.clear();
        offset = 0;
        final_lista = false;
        mostrar_responsables(dni_usuario, offset, max);
    }

}