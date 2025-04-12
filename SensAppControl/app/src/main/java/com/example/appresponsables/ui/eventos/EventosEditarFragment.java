// Este archivo define el fragmento `EventosEditarFragment`.
// Permite a los usuarios responsables editar eventos ya creados.
// Para esto se incluyen un método para obtener los nombres de los eventos ya existentes
// y otro para actualizar el evento.


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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.appresponsables.databinding.FragmentEditarEventosBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventosEditarFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    String stringfecha;
    JSONObject EventoObject;
    String dni_usuario;
    String nombre;
    String tipo;
    Boolean completado;
    Button boton_editar;
    Evento evento;
    List<String> nombres_eventos;
    private NavController navController;
    private FragmentEditarEventosBinding binding;
    private DatePicker datePicker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentEditarEventosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_responsables);

        // Obtenemos la actividad y los datos del Bundle (evento que se quiere editar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity !=null) {
            intent = activity.getIntent();
            if(bundle!=null) {
            stringjson = bundle.getString("eventoObject");
                Log.d("EventosFragment", "stringjson: " + stringjson);

                try {
                    EventoObject = new JSONObject(stringjson);
                    dni_usuario = EventoObject.getString("dni_usuario");
                    nombre = EventoObject.getString("nombre");
                    tipo = EventoObject.getString("tipo");
                    completado = EventoObject.getBoolean("completado");
                    stringfecha = EventoObject.getString("dia");
                    evento = new Evento(dni_usuario, nombre, tipo, LocalDate.parse(stringfecha), completado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.e("EventosEditarFragment", "El string JSON es null");
            }
        }
        // Se crea una lista con los nombres de los eventos ya existentes para dni_usuario
        nombres_eventos = new ArrayList<>();
        listar_nombres();

        // Configuramos los datos del evento a a editar en pantalla
        ImageView imagen = binding.imagenEvento;
        int foto = ObtenerIcono(tipo);
        imagen.setImageResource(foto);
        EditText textonombre = binding.nombreEvento;
        textonombre.setText(nombre);
        // Configuramos las opciones del Spinner
        Spinner spinner = binding.spinnerTipoEvento;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.tipos_eventos_array, R.layout.spinner_tipo_alarma);
        adapter.setDropDownViewResource(R.layout.spinner_despliegue);
        spinner.setAdapter(adapter);
        // Configuramos el DatePicker para mostrar la fecha del evento
        datePicker = binding.datePicker;
        LocalDate fechaEvento = evento.getDia();
        datePicker.updateDate(fechaEvento.getYear(), fechaEvento.getMonthValue() - 1, fechaEvento.getDayOfMonth());
        // Configuramos el Spinner para mostrar el tipo de evento
        String[] tiposArray = requireContext().getResources().getStringArray(R.array.tipos_eventos_array);
        int pos = -1;
        for (int i=0; i< tiposArray.length; i++)
            if (tiposArray[i].equals(tipo)) {
                pos = i;
            }
        if(pos != -1){
            spinner.setSelection(pos);
        }

        // Configuramos el botón de editar evento
        boton_editar = binding.botonEditarEvento;
        boton_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Funcionamiento del botón:
                // Obtenemos los nuevos datos del evento
                String txnombre = binding.nombreEvento.getText().toString();
                String txtipo = spinner.getSelectedItem().toString();
                int dia = binding.datePicker.getDayOfMonth();
                int mes = binding.datePicker.getMonth() +1;
                int ano = binding.datePicker.getYear();
                String txfecha = String.format("%04d-%02d-%02d", ano, mes,dia);
                Log.d("EventosFragment", "STRING DEL DATEPICKER: " + txfecha);

                // Comprobamos que el nombre sea correcto (ni vacío ni repetido) y editamos el evento si lo es
                if(nombres_eventos.contains(txnombre) && !txnombre.equals(evento.getNombre())){
                    Toast.makeText(activity.getApplicationContext(), "Ya existe un evento con ese nombre", Toast.LENGTH_SHORT).show();
                }else if (txnombre.equals("")) {
                    Toast.makeText(activity.getApplicationContext(), "Introduce un nombre!", Toast.LENGTH_SHORT).show();
                }else {
                    actualizar_evento(evento, txnombre, txfecha, txtipo);
                }
            }
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    // Método para editar un evento, enviando la solicitud al servidor
    public void actualizar_evento(Evento evento, String nombre, String txfecha, String tipo){

        // Creamos el JSON que vamos a enviar
        JSONObject new_evento= new JSONObject();
        try {
            new_evento.put("dni_usuario", evento.getDni_usuario());
            new_evento.put("nombre",nombre);
            new_evento.put("tipo",tipo);
            Log.d("EventosFragment", "Formato de fecha: " + txfecha);

            new_evento.put("dia", txfecha);
            new_evento.put("completado",evento.getCompletado());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"evento?nombre=" + evento.getNombre()+ "&dni_usuario=" + dni_usuario;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_evento,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Evento actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que el evento  se actualice correctamente, se navega al fragment EventosEditarFragment
                                // pasando el evento editado en el bundle.
                                Log.d("EventosFragment", "CONTROL");
                                Log.d("EventosFragment", "JSON de eventoMANDADO: " + LocalDate.parse(txfecha).toString());
                                Evento eventofinal = new Evento(evento.getDni_usuario(), nombre, tipo, LocalDate.parse(txfecha), evento.getCompletado());
                                Log.d("EventosFragment", "CONTROL2222222");
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(eventofinal);
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
        //Mandamos la solicitud
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



    // Método para mostrar la imagen correcta dependiendo del tipo de evento
    public int ObtenerIcono(String tipo){
        switch (tipo){
            case "Cita Médica":
                return R.drawable.evento_cita_medica;
            case "Reunión Familiar":
                return R.drawable.evento_reunion_familiar;
            case "Evento Deportivo":
                return R.drawable.evento_evento_desportivo;
            case "Celebración":
                return R.drawable.evento_celebracion;
            case "Cumpleaños":
                return R.drawable.evento_cumpleanos;
            case "Visita Domiciliaria":
                return R.drawable.evento_visita_domiciliaria;
            case "Tarea Doméstica":
                return R.drawable.evento_tarea_domestica;
            case "Evento Religioso":
                return R.drawable.evento_evento_religioso;
            case "Renovación Documentos":
                return R.drawable.evento_renovacion;
            case "Trámites":
                return R.drawable.evento_tramites;
            case "Visita":
                return R.drawable.evento_visita;
            case "Medicamento":
                return R.drawable.evento_medicamento;
            case "Actividad Física":
                return R.drawable.evento_actividad_fisica;
            case "Recado":
                return R.drawable.evento_recado;
            case "Entretenimiento":
                return R.drawable.evento_entretenimiento;
            case "Viaje":
                return R.drawable.evento_viaje;
            case "Reunión":
                return R.drawable.evento_reunion;
            case "Veterinario":
                return R.drawable.evento_veterinario;
            case "Compra":
                return R.drawable.evento_compra;
            case "Otro":
                return R.drawable.evento_otro;
            default:
                return R.drawable.evento_otro;
        }
    }

}