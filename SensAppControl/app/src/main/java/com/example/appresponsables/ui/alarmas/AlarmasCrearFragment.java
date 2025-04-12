// Este archivo define el fragmento `AlarmasCrearFragment`.
// Permite a los usuarios responsables crear nuevas alarmas.
// Para esto se incluyen un método para obtener los nombres de las alarmas ya existentes
// y otro para enviar la nueva alarma creada.

package com.example.appresponsables.ui.alarmas;

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
import android.widget.TimePicker;
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
import com.example.appresponsables.Alarma;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentCrearAlarmasBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class AlarmasCrearFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    Button boton_crear;
    List <String> nombres_alarmas;
    private NavController navController;
    private FragmentCrearAlarmasBinding binding;
    private TimePicker timePicker;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentCrearAlarmasBinding.inflate(inflater, container, false);
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
        // Se crea una lista con los nombres de las alarmas ya existentes para dni_usuario
        nombres_alarmas = new ArrayList<>();
        listar_nombres();

        // Configuramos el Spinner para elegir el tipo de alarma
        Spinner spinner = binding.spinnerTipoAlarma;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.tipos_array, R.layout.spinner_tipo_alarma);
        adapter.setDropDownViewResource(R.layout.spinner_despliegue);
        spinner.setAdapter(adapter);

        // Configuramos el TimePicker para seleccionar la hora de la alarma
        timePicker = binding.timePicker;
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) ->{

        });

        // Configuramos el botón de crear nueva alarma
        boton_crear = binding.botonCrearAlarma;
        boton_crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Funcionamiento del botón

                // Obtenemos los datos de la nueva alarma
                String txnombre = binding.nombreAlarma.getText().toString();
                String txtipo = spinner.getSelectedItem().toString();
                int hora = binding.timePicker.getHour();
                int minutos = binding.timePicker.getMinute();
                String txhora = String.format("%02d:%02d:00", hora, minutos).trim();
                Log.d("AlarmasFragment", "STRING DEL TIMEPICKER: " + txhora);
                Log.d("AlarmasFragment", "STRING DEL TIMEPICKER (length " + txhora.length() + "): '" + txhora + "'");

                Alarma alarma1 = new Alarma(dni_usuario, txnombre, txtipo, LocalTime.parse(txhora), LocalDate.of(2020,1,1));
                Log.d("AlarmasFragment", "STRING DE la alarma: " + alarma1.toString());

                // Comprobamos que el nombre sea correcto (ni vacío ni repetido) y creamos la alarma si lo es
                if(nombres_alarmas.contains(txnombre)){
                    Toast.makeText(activity.getApplicationContext(), "Ya existe una alarma con ese nombre", Toast.LENGTH_SHORT).show();
                }else if (txnombre.equals("")) {
                    Toast.makeText(activity.getApplicationContext(), "Introduce un nombre!", Toast.LENGTH_SHORT).show();
                }else{
                        crear_alarma(alarma1, txhora);
                }
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    // Método para crear una nueva alarma, enviando la solicitud al servidor
    public void crear_alarma(Alarma alarma, String hora){

        // Creamos el JSON que vamos a enviar
        JSONObject new_alarma= new JSONObject();
        try {
            new_alarma.put("dni_usuario", alarma.getDni_usuario());
            new_alarma.put("nombre",alarma.getNombre());
            new_alarma.put("tipo",alarma.getTipo());
            Log.d("AlarmasFragment", "STRING que se usa para el value of: " + alarma.getHora().toString());
            new_alarma.put("hora", hora);
            new_alarma.put("completado", alarma.getCompletado().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"alarma";

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_alarma,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Insertado")) {
                                // Toast.makeText(activity.getApplicationContext(), "Alarma creada", Toast.LENGTH_SHORT).show();
                                // En caso de que la alarma se cree correctamente, se navega al fragment AlarmasEditarFragment
                                // pasando la alarma creada en el bundle.
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalTime(new GsonBuilder()).create();
                                String json = gson.toJson(alarma);
                                bundle.putString("alarmaObject", json);
                                Log.d("AlarmasFragment", "JSON de alarmaMANDADO: " + json);
                                navController.popBackStack();
                                navController.navigate(R.id.nav_alarmas_editar, bundle);
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
                        "Se ha producido un error actualizando  la alarma", Toast.LENGTH_SHORT).show();
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

    // Método para obtener los nombres de todas las alarmas de un dni_usuario, enviando la solicitud al servidor
    public void listar_nombres(){
        final String URL = getString(R.string.IP) + "alarma/lista?dni_usuario=" + dni_usuario;
        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todos los nombres de las alarmas recibidas a una lista
                        try {
                            nombres_alarmas.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject alarma = response.getJSONObject(i);
                                String nombre = alarma.getString("nombre");
                                nombres_alarmas.add(nombre);
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
