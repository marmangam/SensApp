// Este archivo define el fragmento `AtendiendoFragment`.
// Permite a los operadores ver los detalles de la incidencia que están atendiendo
// Para esto se incluyen una opción para rechazar la incidencia, en caso de que no puedan atenderla
// otra para guardar los detalles de la incidencia (pudiendola dar por completada si se marca en el checkbox)
// y otra para realizar una llamada al número de teléfono del usuario al que corresponde la incidencia.

package com.example.apptrabajadores.ui.atender;

//Imports
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.Incidencia;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentAtendiendoBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class AtendiendoFragment extends Fragment {
    // Variables de la clase
    Activity activity;
    Intent intent;
    String stringjson;
    String stringfecha;
    String stringhora;
    JSONObject IncidenciaObject;
    String dni_usuario;
    long id;
    String operador;
    String descripcion;
    String procedimiento;
    Boolean resuelta;
    Incidencia incidencia;
    String nombre_usuario;
    String apellidos_usuario;
    private NavController navController;
    TextView textonombre;
    private FragmentAtendiendoBinding binding;
    JSONObject UsuarioObject;
    String usuario;
    String nombre;
    String apellidos;
    String password;
    Integer telefono;
    Boolean ocupado = Boolean.FALSE;
    Button boton_rechazar;
    Button boton_guardar;
    Button boton_llamar;
    CheckBox micheck;
    private static final int PERMISO_LLAMADA = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentAtendiendoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_operadores);
        // Obtenemos la actividad, los datos del intent (operador que atiende la incidencia) y los del bundle (incidencia atendida)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity !=null) {
            intent = activity.getIntent();
            if(bundle!=null) {
                stringjson = bundle.getString("incidenciaObject");
                stringhora = bundle.getString("horastring");
                try {
                    IncidenciaObject = new JSONObject(stringjson);
                    id = IncidenciaObject.getLong("id");
                    dni_usuario = IncidenciaObject.getString("dni_usuario");
                    operador = IncidenciaObject.getString("operador");
                    descripcion = IncidenciaObject.getString("descripcion");
                    procedimiento = IncidenciaObject.getString("procedimiento");
                    resuelta = IncidenciaObject.getBoolean("resuelta");
                    stringfecha = IncidenciaObject.getString("fecha");
                    incidencia = new Incidencia(id, dni_usuario, operador, descripcion, procedimiento, resuelta, LocalDate.parse(stringfecha), LocalTime.parse(stringhora));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                usuario = UsuarioObject.getString("usuario");
                nombre = UsuarioObject.getString("nombre");
                apellidos = UsuarioObject.getString("apellidos");
                password = UsuarioObject.getString("contrasena");
                ocupado = UsuarioObject.getBoolean("ocupado");
                incidencia.setOperador(usuario);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Configuramos los datos de la incidencia en pantalla
        TextView textdni = binding.textDNIUsuario;
        textdni.setText(dni_usuario);
        textonombre = binding.textNombreUsuario;
        consultar_usuario(dni_usuario);
        if(incidencia.getResuelta()){
            binding.linearLayoutId.setBackgroundResource(R.color.resuelta);
        }else{
            binding.linearLayoutId.setBackgroundResource(R.color.noresuelta);
        }
        TextView textoid= binding.textId;
        textoid.setText(textoid.getText().toString() + String.valueOf(incidencia.getId()));
        TextView textofecha = binding.textFecha;
        textofecha.setText(incidencia.getFecha().toString());
        TextView textohora = binding.textHora;
        textohora.setText(incidencia.getHora().toString());
        TextView textodesc = binding.textDescripcion;
        if (!(incidencia.getDescripcion() == null ||incidencia.getDescripcion().equalsIgnoreCase("null"))) {
            textodesc.setText(incidencia.getDescripcion());
        }
        TextView textoproc = binding.textProcedimiento;
        if (!(incidencia.getProcedimiento() == null ||incidencia.getProcedimiento().equalsIgnoreCase("null"))) {
            textoproc.setText(incidencia.getProcedimiento());
        }
        TextView textooperador = binding.textOperador;
        textooperador.setText(textooperador.getText().toString() + incidencia.getOperador());
        micheck = binding.checkBox;
        micheck.setChecked(incidencia.getResuelta());


        // Configuramos el botón rechazar
        boton_rechazar = binding.botonRechazar;
        boton_rechazar.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos, actualizamos el operador y la incidencia
            String nuevadescripcion = binding.textDescripcion.getText().toString();
            String nuevoprocedimiento = binding.textProcedimiento.getText().toString();
            Boolean rechazar = Boolean.TRUE;
            Boolean completada = Boolean.FALSE;
            actualizar_operador(incidencia.getOperador(), nombre, apellidos, password, rechazar, completada);
            actualizar_incidencia(nuevadescripcion, nuevoprocedimiento, incidencia,completada, rechazar);
        });

        // Configuramos el botón guardar
        boton_guardar = binding.botonGuardar;
        boton_guardar.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos, actualizamos el operador y la incidencia
            String nuevadescripcion = binding.textDescripcion.getText().toString();
            String nuevoprocedimiento = binding.textProcedimiento.getText().toString();
            if (nuevadescripcion == null ||nuevadescripcion.equalsIgnoreCase("null")) {
                nuevadescripcion = "";
            }
            if (nuevoprocedimiento == null ||nuevoprocedimiento.equalsIgnoreCase("null")) {
                nuevoprocedimiento = "";
            }
            Boolean completada = binding.checkBox.isChecked();
            Boolean rechazar = Boolean.FALSE;
            actualizar_operador(incidencia.getOperador(), nombre, apellidos, password, rechazar, completada);
            actualizar_incidencia(nuevadescripcion, nuevoprocedimiento, incidencia, completada, rechazar);

        });


        // Configuramos el botón llamar
        boton_llamar = binding.botonLlamar;
        boton_llamar.setOnClickListener(v -> {
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos, actual

            // Se verifica si se tiene el permiso del usuario para hacer la llamada
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                Log.d("LLAMAR", "se hace click en llamar, el telefono es: " + telefono);
                // Si hay permiso se realiza la llamada si hay una aplicación válida para ello
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + telefono));
                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                }
            } // Si no se tiene el permiso del usuario se solicita
            else {
                Toast.makeText(getContext(), "Debes conceder el permiso para realizar una llamada!", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, PERMISO_LLAMADA);
            }
        });



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    // Método para consultar datos sobre el usuario de la incidencia, enviando la solicitud al servidor
    public void consultar_usuario(String dni_usuario){
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                nombre_usuario = response.getString("nombre");
                                apellidos_usuario = response.getString("apellidos");
                                telefono = response.getInt("telefono");

                                textonombre.setText(nombre_usuario + " " + apellidos_usuario);
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


    // Método para editar un operador, enviando la solicitud al servidor
    public void actualizar_operador(String usuario,String nombre, String apellidos, String password, Boolean rechazar, Boolean completada){

        // Creamos el JSON que vamos a enviar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario",  usuario);
            new_operador.put("nombre", nombre);
            new_operador.put("apellidos", apellidos);
            new_operador.put("contrasena",password);
            new_operador.put("activo", Boolean.TRUE);
            if(completada || rechazar){
                new_operador.put("ocupado", Boolean.FALSE);
            }else{
                new_operador.put("ocupado", Boolean.TRUE);
            }
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
                                //Se ha actualizado correctamente
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


    // Método para actualizar una incidencia, enviando la solicitud al servidor
    public void actualizar_incidencia(String descripcion, String procedimiento, Incidencia incidencia, Boolean completada, Boolean rechazar){
        // Creamos el JSON que vamos a mandar
        JSONObject new_incidencia= new JSONObject();
        try {
            new_incidencia.put("id", incidencia.getId());
            new_incidencia.put("dni_usuario", incidencia.getDni_usuario());
            if(rechazar){
                new_incidencia.put("operador", JSONObject.NULL);
            }else{
                new_incidencia.put("operador", incidencia.getOperador());
            }
            new_incidencia.put("resuelta", completada);
            new_incidencia.put("fecha", incidencia.getFecha().toString());
            String txhora = String.format("%02d:%02d:%02d", incidencia.getHora().getHour(),incidencia.getHora().getMinute(),incidencia.getHora().getSecond());
            Log.d("DEBUG", "Hora objeto: " + incidencia.getHora());
            Log.d("DEBUG2", "txhora: " + txhora);
            new_incidencia.put("hora", txhora);
            new_incidencia.put("descripcion", descripcion);
            new_incidencia.put("procedimiento", procedimiento);
            Log.d("MainUsuarios", "STRING DE la incidencia: " + new_incidencia.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"incidencia/" + incidencia.getId();

        // Mandamos la solcitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_incidencia,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                Log.d("AtendiendoFragment", "Incidencia actualizada correctamente: " + response.toString());
                                //Toast.makeText(activity.getApplicationContext(), "Incidencia actualizado", Toast.LENGTH_SHORT).show();
                                // La incidencia se ha actualizado correctamente

                                // Se manda en el bundle el operador que está  utilizando la aplicación
                                incidencia.setResuelta(completada);
                                Bundle bundle = new Bundle();
                                bundle.putString("jsonObject", intent.getStringExtra("jsonObject"));
                                //Si se ha pulsado rechazar se navega al fragmento AtenderFragment pasando el bundle
                                if(rechazar){
                                    navController.popBackStack();
                                    navController.navigate(R.id.nav_atender, bundle);
                                }else{
                                    if(completada){
                                        //Si se ha completado la incidencia se navega al fragmento AtenderFragment pasando el bundle
                                        navController.popBackStack();
                                        navController.navigate(R.id.nav_atender, bundle);
                                    } // Si no, se añade la incidencia actualizada al bundle y se navega al fragmento AtendiendoFragment
                                    else {
                                        Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                        Incidencia incidenciafinal = new Incidencia(incidencia.getId(), incidencia.getDni_usuario(), rechazar ? "" : incidencia.getOperador(), descripcion,
                                                procedimiento, completada, incidencia.getFecha(), incidencia.getHora());
                                        String json = gson.toJson(incidenciafinal);
                                        bundle.putString("incidenciaObject", json);
                                        bundle.putString("horastring", incidenciafinal.getHora().toString());
                                        Log.d("IncidenciasFragment", "JSON de la incidencia: " + json);

                                        navController.popBackStack();
                                        navController.navigate(R.id.nav_atendiendo, bundle);
                                    }
                                }
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
                        "Se ha producido un error actualizando la incidencia", Toast.LENGTH_SHORT).show();
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

    // Método para manejar la respuesta del usuario a la solicitud de permisos para hacer una llamada
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISO_LLAMADA) {
            // Si el usuario lo ha permitido se hace la llamda
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LLAMAR", "se hace click en llamar, el telefono es: " + telefono);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + telefono));
                startActivity(intent);
            }
        }
    }



}