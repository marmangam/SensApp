// Este archivo define el fragmento `AlarmasEditarFragment`.
// Permite a los usuarios responsables editar alarmas ya creadas.
// Para esto se incluyen un método para obtener los nombres de las alarmas ya existentes
// y otro para actualizar la alarma.

package com.example.appresponsables.ui.alarmas;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.util.Log;
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
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentEditarAlarmasBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.appresponsables.R;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class AlarmasEditarFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    String stringhora;
    JSONObject AlarmaObject;
    String dni_usuario;
    String nombre;
    String tipo;
    LocalDate completado;
    Button boton_editar;
    Alarma alarma;
    List <String> nombres_alarmas;
    private NavController navController;
    private FragmentEditarAlarmasBinding binding;
    private TimePicker timePicker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentEditarAlarmasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_responsables);

        // Obtenemos la actividad y los datos del Bundle (alarma que se quiere editar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity !=null) {
            intent = activity.getIntent();
            if(bundle!=null) {
            stringjson = bundle.getString("alarmaObject");
            //stringhora = bundle.getString("hora");
                Log.d("AlarmasFragment", "stringjson: " + stringjson);
              //  Log.d("AlarmasFragment", "stringhora: " + stringhora);

                try {
                    AlarmaObject = new JSONObject(stringjson);
                    dni_usuario = AlarmaObject.getString("dni_usuario");
                    nombre = AlarmaObject.getString("nombre");
                    tipo = AlarmaObject.getString("tipo");
                    String txcompletado = AlarmaObject.getString("completado");
                    if (txcompletado !=null && !txcompletado.trim().isEmpty() && !txcompletado.equals("{}")) {
                        completado = LocalDate.parse(txcompletado);
                    }else{
                        completado=LocalDate.of(2020, 1,1);
                    }
                    stringhora = AlarmaObject.getString("hora");
                    alarma = new Alarma(dni_usuario, nombre, tipo, LocalTime.parse(stringhora), completado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.e("AlarmasEditarFragment", "El string JSON es null");
            }
        }
        // Se crea una lista con los nombres de las alarmas ya existentes para dni_usuario
        nombres_alarmas = new ArrayList<>();
        listar_nombres();
        Log.d("AlarmasFragment", "STRING DE la lista de nombres " + nombres_alarmas);

        // Configuramos los datos de la alarma a editar en pantalla
        ImageView imagen = binding.imagenAlarma;
        int foto = ObtenerIcono(tipo);
        imagen.setImageResource(foto);
        EditText textonombre = binding.nombreAlarma;
        textonombre.setText(nombre);
        // Configuramos las opciones del Spinner
        Spinner spinner = binding.spinnerTipoAlarma;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.tipos_array, R.layout.spinner_tipo_alarma);
        adapter.setDropDownViewResource(R.layout.spinner_despliegue);
        spinner.setAdapter(adapter);
        // Configuramos el TimePicker para mostrar la hora de la alarma
        timePicker = binding.timePicker;
        String[] partes = stringhora.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto= Integer.parseInt(partes[1]);
        timePicker.setHour(hora);
        timePicker.setMinute(minuto);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) ->{
        });
        // Configuramos el Spinner para mostrar el tipo de alarma
        String[] tiposArray = requireContext().getResources().getStringArray(R.array.tipos_array);
        int pos = -1;
        for (int i=0; i< tiposArray.length; i++)
            if (tiposArray[i].equals(tipo)) {
                pos = i;
            }
        if(pos != -1){
            spinner.setSelection(pos);
        }

        // Configuramos el botón de editar alarma
        boton_editar = binding.botonEditarAlarma;
        boton_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Funcionamiento del botón

                // Obtenemos los nuevos datos de la alarma
                String txnombre = binding.nombreAlarma.getText().toString();
                String txtipo = spinner.getSelectedItem().toString();
                int hora = binding.timePicker.getHour();
                int minutos = binding.timePicker.getMinute();
                String txhora = String.format("%02d:%02d:00", hora, minutos);
                Log.d("AlarmasFragment", "STRING DEL TIMEPCIKER: " + txhora);

                // Comprobamos que el nombre sea correcto (ni vacío ni repetido) y editamos la alarma si lo es
                if(nombres_alarmas.contains(txnombre) && !txnombre.equals(alarma.getNombre())){
                    Toast.makeText(activity.getApplicationContext(), "Ya existe una alarma con ese nombre", Toast.LENGTH_SHORT).show();
                }else if (txnombre.equals("")) {
                    Toast.makeText(activity.getApplicationContext(), "Introduce un nombre!", Toast.LENGTH_SHORT).show();
                }else {
                    actualizar_alarma(alarma, txnombre, txhora, txtipo);
                }
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    // Método para editar una alarma, enviando la solicitud al servidor
    public void actualizar_alarma(Alarma alarma, String nombre, String txhora, String tipo){

        // Creamos el JSON que vamos a enviar
        JSONObject new_alarma= new JSONObject();
        try {
            new_alarma.put("dni_usuario", alarma.getDni_usuario());
            new_alarma.put("nombre",nombre);
            new_alarma.put("tipo",tipo);
            new_alarma.put("hora", Time.valueOf(txhora));
            new_alarma.put("completado",alarma.getCompletado());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"alarma?nombre=" + alarma.getNombre()+ "&dni_usuario=" + dni_usuario;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_alarma,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Alarma actualizada", Toast.LENGTH_SHORT).show();
                                // En caso de que la alarma se actualice correctamente, se navega al fragment AlarmasEditarFragment
                                // pasando la alarma editada en el bundle.
                                Alarma alarmafinal = new Alarma(alarma.getDni_usuario(), nombre, tipo, LocalTime.parse(txhora), alarma.getCompletado());
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalTime(new GsonBuilder()).create();
                                String json = gson.toJson(alarmafinal);
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

    // Método para mostrar la imagen correcta dependiendo del tipo de alarma
    public int ObtenerIcono(String tipo) {
        switch (tipo) {
            case "Medicamento":
                return R.drawable.alarma_medicamento;
            case "Pomada":
                return R.drawable.alarma_pomada;
            case "Pastilla":
                return R.drawable.alarma_pastilla;
            case "Suplementos":
                return R.drawable.alarma_suplementos;
            case "Ejercicio":
                return R.drawable.alarma_ejercicio;
            case "Control Tensión":
                return R.drawable.alarma_control_tension;
            case "Control Glucosa":
                return R.drawable.alarma_control_glucosa;
            case "Cuidado personal":
                return R.drawable.alarma_cuidado_personal;
            case "Revisar Enchufes":
                return R.drawable.alarma_enchufe;
            case "Revisar Grifos":
                return R.drawable.alarma_grifos;
            case "Alimentar mascota":
                return R.drawable.alarma_alimentar_mascota;
            case "Otros":
                return R.drawable.alarma_otro;
            default:
                return R.drawable.alarma_otro;
        }
    }

}