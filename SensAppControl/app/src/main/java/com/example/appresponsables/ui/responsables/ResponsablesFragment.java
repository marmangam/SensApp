// Este archivo define el fragmento `ResponsablesFragment`.
// Permite a los usuarios responsables ver todos los responsables asociados existentes(paginación) (nombre completo y número de teléfono)
// Para esto se incluyen un método para obtener todos los responsables,

package com.example.appresponsables.ui.responsables;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.appresponsables.Responsable;
import com.example.appresponsables.ListaAdaptadorResponsables;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentResponsablesBinding;
import com.example.appresponsables.utilidades.MarginItemDecoration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResponsablesFragment extends Fragment {
    // Variables de la clase
    private ListaAdaptadorResponsables adaptador;
    private List<Responsable> responsables;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    private NavController navController;
    private FragmentResponsablesBinding binding;
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
        // Configuramos la vista para mostrar los responsables en una matriz de 1 columna
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_responsables);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_evento_margen);
        recyclerView.addItemDecoration(new MarginItemDecoration(margen));
        responsables = new ArrayList<>();
        // Definimos un ListaAdaptadorResponsables para mostrar la lista
        adaptador = new ListaAdaptadorResponsables(responsables);
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
        return view;

    }


    // Método para obtener max responsables de un dni_usuario, enviando la solicitud al servidor
    public void mostrar_responsables(String dni_usuario, int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("ResponsablesFragment", "SE LLAMA A MOSTRAR RESPONSABLES, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "responsable/lista/" + dni_usuario + "?offset=" + offset + "&limit=" + max;
            // Mandamos la solicitud
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
                                Log.d("ResponsablesFragment", "Cantidad agregados nuevos: " + add);
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
}