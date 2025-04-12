// Este archivo define el fragmento `AtenderFragment`.
// Permite a los operadores ver todas las incidencias que existen actualmente sin resolver y que no están siendo atendidas.
// Para esto se incluyen un método para obtener todas las incidencias no resueltas y sin operador, se implementa paginación.
// También permite navegar al fragmento responsable de atender una incidencia pulsando en la incidencia deseada.

package com.example.apptrabajadores.ui.atender;

//Imports
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.Incidencia;
import com.example.apptrabajadores.ListaAdaptadorIncidencias;
import com.example.apptrabajadores.Operador;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentAtenderBinding;
import com.example.apptrabajadores.utilidades.MarginItemDecoration;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;


public class AtenderFragment extends Fragment {
    // Variables de la clase
    private FragmentAtenderBinding binding;
    private ListaAdaptadorIncidencias adaptador;
    private List<Incidencia> incidencias = new ArrayList<>();
    private List<Incidencia> incidencias_operador = new ArrayList<>();
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String usuario;
    String nombre;
    String apellidos;
    String password;
    Boolean ocupado = Boolean.FALSE;
    private NavController navController;
    Set<Integer> incidencias_id = new HashSet<>();
    int offset = 0;
    int max = 10;
    boolean cargando = false;
    boolean final_lista = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentAtenderBinding.inflate(inflater, container, false);
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
                usuario = UsuarioObject.getString("usuario");
                nombre = UsuarioObject.getString("nombre");
                apellidos = UsuarioObject.getString("apellidos");
                password = UsuarioObject.getString("contrasena");
                ocupado = UsuarioObject.getBoolean("ocupado");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Si el operador esta ocupado obtenemos la incidencia que está atendiendo
        if(ocupado){
            obtener_incidencia_oper(usuario, new OnIncidenciasListener() {
                @Override
                public void onSuccess(List<Incidencia> listaIncidencias) {
                    if (!incidencias_operador.isEmpty()) {
                        Incidencia incidencia = incidencias_operador.get(0);
                        Bundle bundle = new Bundle();
                        Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                        String json = gson.toJson(incidencia);
                        bundle.putString("incidenciaObject", json);
                        bundle.putString("horastring", incidencia.getHora().toString());
                        // Añadimos al bundle la incidencia que está siendo atendida y navegamos al fragmento AtendiendoFragment
                        navController.navigate(R.id.action_nav_atender_to_nav_atendiendo, bundle);
                    }
                }
                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(getContext(), "Error al obtener incidencia activa", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Configuramos la vista para mostrar las incidencias en una matriz de 1 columna
        binding.recyclerViewIncidencias.setLayoutManager(new GridLayoutManager(getContext(), 1));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_usuario_margen);
        binding.recyclerViewIncidencias.addItemDecoration(new MarginItemDecoration(margen));
        // Definimos un ListaAdaptadorIncidencias para mostrar la lista
        adaptador = new ListaAdaptadorIncidencias(incidencias, new ListaAdaptadorIncidencias.OnIncidenciaClickListener() {
            @Override
            public void onIncidenciaClick(Incidencia incidencia) {
                // Funcionamiento al hacer click en una incidencia:
                // Se comprueba  que no esté siendo atendida por ningún operador y en caso de éxito
                // Añadimos al bundle la incidencia seleccionada y navegamos al fragmento AtendiendoFragment
                consultar_incidencia(incidencia.getId(), new OnIncidenciaListener() {
                    @Override
                    public void onResultado(Incidencia incidenciafinal) {
                        Log.d("DEBUG", "Valor de operador: '" + incidenciafinal.getOperador() + "'");
                        if (incidenciafinal.getOperador() == null || incidenciafinal.getOperador().isEmpty() || incidenciafinal.getOperador().equalsIgnoreCase("null")) {
                            ocupar_operador(usuario, nombre, apellidos, password);
                            actualizar_incidencia(incidencia, usuario);
                            //IR AL OTRO FRAGMENTO
                            Bundle bundle = new Bundle();
                            Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                            String json = gson.toJson(incidenciafinal);
                            bundle.putString("incidenciaObject", json);
                            bundle.putString("horastring", incidenciafinal.getHora().toString());
                            Log.d("IncidenciasFragment", "JSON de la incidencia: " + json);

                            navController.navigate(R.id.action_nav_atender_to_nav_atendiendo, bundle);

                        } //En  caso de error (la incidencia tiene operador, está siendo atendida), mostramos un diálogo
                        // indicando esto y recargamos las incidencias mostradas
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                            TextView titulo = new TextView(getContext());
                            titulo.setText("Incidencia atendida");
                            titulo.setTextSize(30);
                            titulo.setPadding(25, 25, 25, 25);
                            titulo.setTypeface(null, Typeface.BOLD);
                            titulo.setGravity(Gravity.CENTER);
                            builder.setCustomTitle(titulo);

                            TextView mensaje = new TextView(getContext());
                            mensaje.setText("Esta incidencia ya está siendo atendida por otro operador.");
                            mensaje.setTextSize(20);
                            mensaje.setPadding(25, 25, 25, 25);
                            mensaje.setGravity(Gravity.CENTER);
                            builder.setView(mensaje);

                            builder.setPositiveButton("OK", (dialog, which) -> {
                                incidencias.clear();
                                mostrar_incidencias(max, offset);
                            });
                            AlertDialog dialogo = builder.create();
                            dialogo.show();

                            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
                        }
                    }
                });
            }

        });


        // Seleccionamos el adaptador creado
        binding.recyclerViewIncidencias.setAdapter(adaptador);

        // Definimos el comportamiento al hacer scroll en la lista para implementar paginación
        binding.recyclerViewIncidencias.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!(cargando || final_lista)) {
                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                    // Si se ha llegado al final de los elementos se cargan más
                    if (manager != null && manager.findLastVisibleItemPosition() == incidencias.size() - 1) {
                        offset += max;
                        Log.d("ATENDER", "SE HACE SCROLL, OFFSET: " + offset);
                        mostrar_incidencias(offset, max);
                    }
                }
            }
        });

        // Mostramos las primeras incidencias
        mostrar_incidencias(offset, max);


        return view;

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // Método para obtener max incidencias, enviando la solicitud al servidor
    public void mostrar_incidencias(int offset, int max) {
        // Se comprueba que no se esté cargando otra solicitud y que no se haya llegado al final de la lista
        Log.d("ATENDER", "SE LLAMA A MOSTRAR INCIDENCIAS, OFFSET: " + offset + " LIMIT: " + max);
        if (!(cargando || final_lista)) {
            // Empezamos a cargar la solicitud
            cargando = true;
            final String URL = getString(R.string.IP) + "incidencia/noresuelta?offset=" + offset + "&limit=" + max;

            // Mandamos la solicitud
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            int add = 0;
                            Log.d("ATENDER", "Cantidad en response: " + response.length());
                            //Si llegamos al final de la lista lo indicamos
                            if (response.length() < max) {
                                final_lista = true;
                            }
                            try {
                                // Añadimos todas las incidencias que no estén añadidas ya
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject incidencia = response.getJSONObject(i);
                                    Integer id = incidencia.getInt("id");
                                    if (!incidencias_id.contains(id)) {
                                        incidencias_id.add(id);
                                        String operador = incidencia.getString("operador");
                                        String dni_usuario = incidencia.getString("dni_usuario");
                                        String descripcion = incidencia.getString("descripcion");
                                        String procedimiento = incidencia.getString("procedimiento");
                                        Boolean resuelta = incidencia.getBoolean("resuelta");
                                        String s_dia = incidencia.getString("fecha");
                                        LocalDate fecha = LocalDate.parse(s_dia);
                                        String s_hora = incidencia.getString("hora");
                                        LocalTime hora = LocalTime.parse(s_hora);

                                        incidencias.add(new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta, fecha, hora));
                                        add +=1;

                                    }
                                }
                                Log.d("ATENDER", "Cantidad agregados nuevos: " + add);
                                // Se ordenan por fecha y hora
                                incidencias.sort((a1, a2) -> {
                                    if (a1.getFecha().equals(a2.getFecha())) {
                                        return a1.getHora().compareTo(a2.getHora());
                                    } else {
                                        return a1.getFecha().compareTo(a2.getFecha());
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



    // Método para editar un operador, marcandolo como ocupado, enviando la solicitud al servidor
    public void ocupar_operador(String usuario,String nombre, String apellidos, String password){

        // Creamos el JSON que vamos a enviar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario",  usuario);
            new_operador.put("nombre", nombre);
            new_operador.put("apellidos", apellidos);
            new_operador.put("contrasena",password);
            new_operador.put("activo", Boolean.TRUE);
            new_operador.put("ocupado", Boolean.TRUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"operador/" + usuario;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_operador,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Operador actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente se añade el operador al bundle
                                Operador operadorfinal = new Operador(usuario, nombre, apellidos, password, new_operador.getBoolean("activo"), ocupado);
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(operadorfinal);
                                bundle.putString("operadorObject", json);
                                Log.d("OperadoresFragment", "JSON del usuario: " + json);
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
                        "Se ha producido un error actualizando  el operador", Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }


    // Método para editar una incidencia, enviando la solicitud al servidor
    public void actualizar_incidencia(Incidencia incidencia, String operador){

        // Creamos el JSON que vamos a enviar
        int hora = incidencia.getHora().getHour();
        int minutos = incidencia.getHora().getMinute();
        String txhora = String.format("%02d:%02d:00", hora, minutos);

        JSONObject new_incidencia= new JSONObject();
        try {
            new_incidencia.put("id",  incidencia.getId());
            new_incidencia.put("dni_usuario", incidencia.getDni_usuario());
            new_incidencia.put("operador", operador);
            new_incidencia.put("resuelta", incidencia.getResuelta());
            new_incidencia.put("fecha", incidencia.getFecha().toString());
            new_incidencia.put("hora", Time.valueOf(txhora));
            new_incidencia.put("descripcion","");
            new_incidencia.put("procedimiento","");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"incidencia/" + incidencia.getId();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_incidencia,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Incidencia actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente se añade la incidencia al bundle
                                Incidencia incidenciafinal = new Incidencia(incidencia.getId(), incidencia.getDni_usuario(), usuario, "", "", incidencia.getResuelta(), incidencia.getFecha(), incidencia.getHora());
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(incidenciafinal);
                                bundle.putString("incidenciaObject", json);
                                Log.d("AtenderFragment", "JSON de la incidencia: " + json);
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
                        "Se ha producido un error actualizando  la incidencia", Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }


    // Método para obtener las incidencias no resueltas de un operador, enviando la solicitud al servidor
    public void obtener_incidencia_oper(String operador, AtenderFragment.OnIncidenciasListener listener) {
        final String URL = getString(R.string.IP) + "incidencia/lista/" + operador;

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Añadimos a una lista todas las incidencias no resueltas del operador
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject incidencia = response.getJSONObject(i);
                                Integer id = incidencia.getInt("id");
                                String dni_usuario = incidencia.getString("dni_usuario");
                                String operador = incidencia.getString("operador");
                                String descripcion = incidencia.getString("descripcion");
                                String procedimiento = incidencia.getString("procedimiento");
                                Boolean resuelta = incidencia.getBoolean("resuelta");
                                String s_dia = incidencia.getString("fecha");
                                LocalDate fecha = LocalDate.parse(s_dia);
                                String s_hora = incidencia.getString("hora");
                                LocalTime hora = LocalTime.parse(s_hora);
                                Incidencia nuevaincidencia = new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta, fecha, hora);
                                if (!resuelta) {
                                   incidencias_operador.add(nuevaincidencia);
                                }
                            }
                            listener.onSuccess(incidencias_operador);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
                listener.onError(error);
            }
        });

        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }

    // Interfaz de la clase OnIncidenciasListener para manejar los eventos onSucces y onError
    public interface OnIncidenciasListener {
        void onSuccess(List<Incidencia> listaIncidencias);
        void onError(VolleyError error);
    }

    // Interfaz de la clase OnIncidenciaListener para manejar los eventos onResultado
    public interface OnIncidenciaListener {
        void onResultado(Incidencia incidencia);
    }


    // Método para obtener la incidencia de id pasado por parámetro, enviando la solicitud al servidor
    public void consultar_incidencia(long id, AtenderFragment.OnIncidenciaListener listener) {
            final String URL = getString(R.string.IP) + "incidencia?id=" + id;

            // Mandamos la solicitud
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null && response.length() > 0) {
                                    long id = response.getLong("id");
                                    String dni_usuario = response.getString("dni_usuario");
                                    String operador = response.optString("operador", "");
                                    String descripcion = response.getString("descripcion");
                                    String procedimiento = response.getString("procedimiento");
                                    boolean resuelta = response.getBoolean("resuelta");
                                    LocalDate fecha = LocalDate.parse(response.getString("fecha"));
                                    LocalTime hora = LocalTime.parse(response.getString("hora"));

                                    Incidencia incidencia = new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta, fecha, hora);
                                    //Se deuelve la inciencia como resultado
                                    listener.onResultado(incidencia);
                                }else{
                                    listener.onResultado(null);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onResultado(null);
                            }
                            VolleyLog.v("Response:%n %s", response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                    error.printStackTrace();
                    listener.onResultado(null);
                }
            });

            ServiciosWeb.getInstance().getRequestQueue().add(request);
    }


}