// Este archivo define el fragmento `EventosCrearFragment`.
// Permite a los usuarios responsables crear nuevos eventos.
// Para esto se incluyen un método para obtener los nombres de las eventos ya existentes
// y otro para enviar el nuevo evento creado.


package com.example.appresponsables.ui.eventos;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appresponsables.Evento;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentCrearEventosBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class EventosCrearFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    Button boton_crear;
    Evento evento;
    List<String> nombres_eventos;
    private NavController navController;
    private FragmentCrearEventosBinding binding;
    private DatePicker datePicker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentCrearEventosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
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
        // Se crea una lista con los nombres de los eventos ya existentes para dni_usuario
        nombres_eventos = new ArrayList<>();
        listar_nombres();

        // Configuramos el Spinner para elegir el tipo de evento
        Spinner spinner = binding.spinnerTipoEvento;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.tipos_eventos_array, R.layout.spinner_tipo_alarma);
        adapter.setDropDownViewResource(R.layout.spinner_despliegue);
        spinner.setAdapter(adapter);

        // Configuramos el TimePicker para seleccionar la fecha del evento
        datePicker = binding.datePicker;
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.updateDate(ano, mes, dia);

        // Configuramos el botón de crear nuevo evento
        boton_crear = binding.botonCrearEvento;
        boton_crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Funcionamiento del botón:
                // Obtenemos los datos del nuevo evento
                String txnombre = binding.nombreEvento.getText().toString();
                String txtipo = spinner.getSelectedItem().toString();
                int dia = binding.datePicker.getDayOfMonth();
                int mes = binding.datePicker.getMonth() +1;
                int ano = binding.datePicker.getYear();
                String txfecha = String.format("%04d-%02d-%02d", ano, mes,dia);
                Log.d("EventosFragment", "STRING DEL DATEPICKER: " + txfecha);
                Evento evento1 = new Evento(dni_usuario, txnombre, txtipo, LocalDate.parse(txfecha), Boolean.FALSE);
                Log.d("AlarmasFragment", "STRING DEL evento: " + evento1.toString());

                // Comprobamos que el nombre sea correcto (ni vacío ni repetido) y creamos el evento si lo es
                if(nombres_eventos.contains(txnombre)){
                    Toast.makeText(activity.getApplicationContext(), "Ya existe un evento con ese nombre", Toast.LENGTH_SHORT).show();
                }else if (txnombre.equals("")) {
                    Toast.makeText(activity.getApplicationContext(), "Introduce un nombre!", Toast.LENGTH_SHORT).show();
                }else{
                    crear_evento(evento1, txfecha);
                }
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    // Método para crear un nuevo evento, enviando la solicitud al servidor
    public void crear_evento(Evento evento, String fecha){

        // Creamos el JSON que vamos a enviar
        JSONObject new_evento= new JSONObject();
        try {
            new_evento.put("dni_usuario", evento.getDni_usuario());
            new_evento.put("nombre",evento.getNombre());
            new_evento.put("tipo",evento.getTipo());
            Log.d("EventosFragment", "STRING que se usa para el value of: " + evento.getDia().toString());
            new_evento.put("dia", fecha);
            new_evento.put("completado",evento.getCompletado());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"evento";

        //Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_evento,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getString("message").equals("Insertado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Evento creada", Toast.LENGTH_SHORT).show();
                                // En caso de que el evento se cree correctamente, se navega al fragment EventosEditarFragment
                                // pasando el evento creado en el bundle.
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(evento);
                                bundle.putString("eventoObject", json);
                                Log.d("EventosFragment", "JSON de eventoMANDADO: " + json);
                                navController.popBackStack();
                                navController.navigate(R.id.nav_eventos_editar, bundle);
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
                        "Se ha producido un error actualizando  el evento", Toast.LENGTH_SHORT).show();
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




    // Método para obtener los nombres de todos los eventos de un dni_usuario, enviando la solicitud al servidor
    public void listar_nombres(){
        final String URL = getString(R.string.IP) + "evento/lista?dni_usuario=" + dni_usuario;
        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todos los nombres de los eventos recibidos a una lista
                        try {
                            nombres_eventos.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject evento = response.getJSONObject(i);
                                String nombre = evento.getString("nombre");
                                nombres_eventos.add(nombre);
                            }
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
            }
        });

        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }



}
