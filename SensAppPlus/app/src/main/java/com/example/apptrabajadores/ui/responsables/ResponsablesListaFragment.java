// Este archivo define el fragmento `ResponsablesListaFragment`.
// Permite a los operadores ver una lista de los responsables de un usuario (paginacion)
// Para esto se incluyen un método para obtener todos los responsables de un usuario.

package com.example.apptrabajadores.ui.responsables;

// Imports
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.apptrabajadores.ListaAdaptadorResponsables;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.Responsable;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentListaResponsablesBinding;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResponsablesListaFragment extends Fragment {
    // Variables de la clase
    private FragmentListaResponsablesBinding binding;
    private ListaAdaptadorResponsables adaptador;
    private List<Responsable> responsables;
    Activity activity;
    Intent intent;
    String dni_usuario;
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
        binding = FragmentListaResponsablesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_operadores);
        // Obtenemos la actividad y los datos del bundle (dni del usuarios cuyos responsables queremos listar)
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
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_listaresponsables);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        recyclerView.addItemDecoration(new MarginItemDecoration(margen));
        responsables = new ArrayList<>();
        // Definimos un ListaAdaptadorResponsables para mostrar la lista
        adaptador = new ListaAdaptadorResponsables(responsables);
        // Seleccionamos el adaptador creado y mostramos todos los responsables
        recyclerView.setAdapter(adaptador);

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewListaresponsables.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        Log.d("ResponsablesListaFragment", "SE LLAMA A MOSTRAR RESPONSABLES, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
        final String URL = getString(R.string.IP) + "responsable/lista/" + dni_usuario + "?offset=" + offset + "&limit=" + max;
        Log.d("ResponsablesListaFragment", "URL de la solicitud: " + URL);

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int add = 0;
                        Log.d("ResponsablesListaFragment", "Cantidad en response: " + response.length());
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}