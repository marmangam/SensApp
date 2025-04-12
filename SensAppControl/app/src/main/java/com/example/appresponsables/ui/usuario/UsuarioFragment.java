// Este archivo define el fragmento `UsuarioFragment`.
// Permite a los usuarios responsables ver los datos de su usuario asociado y modificar algunos de estos.
// Para esto se incluyen un método para obtener los datos de un usuario haciendo mediante el dni_usuario
// y otro para actualizar los datos.

package com.example.appresponsables.ui.usuario;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appresponsables.Usuario;
import com.example.appresponsables.R;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentUsuarioBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class UsuarioFragment extends Fragment {
    // Variables de la clase
    private FragmentUsuarioBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    TextView textonombre;
    TextView textodni;
    EditText textotelefono;
    TextView textoedad;
    EditText textodomicilio;
    EditText textoenfermedades;
    EditText textoalergias;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Button ver_responsables;
    Usuario usuario;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentUsuarioBinding.inflate(inflater, container, false);
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

        // Configuramos los datos del usuario en pantalla
        textonombre = binding.textNombre;
        textodni = binding.textDni;
        textotelefono = binding.textTelefono;
        textoedad = binding.textEdad;
        textodomicilio = binding.textdomicilio;
        textoenfermedades = binding.textenfermedades;
        textoalergias = binding.textalergias;
        textopassword = binding.textPassword;
        consultar_usuario();
        // Configuramos la funcionalidad para ver la contraseña al pulsar la imagen
        ver_contrasena = binding.imagenVerPassword;
        ver_contrasena.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent evento) {
                switch (evento.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        textopassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        textopassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        return true;
                }
                return false;
            }
        });

        // Configuramos el botón editar datos
        boton_editar = binding.botonEditarUsuario;
        boton_editar.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos
            String nuevodomicilio = binding.textdomicilio.getText().toString();
            String nuevaenfermedad = binding.textenfermedades.getText().toString();
            String nuevaalergia= binding.textalergias.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            String textotelefono = binding.textTelefono.getText().toString();
            // Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            if(textotelefono.isEmpty() || nuevodomicilio.equals("") || nuevapassword.equals("")) {
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else {
                Integer nuevotelefono = Integer.valueOf(binding.textTelefono.getText().toString());
                actualizar_usuario(usuario, nuevotelefono, nuevodomicilio, nuevaenfermedad, nuevaalergia, nuevapassword);
            }
        });

        // Configuramos el botón ver responsables
        ver_responsables = binding.botonVerResponsables;
        ver_responsables.setOnClickListener(v ->{
            // Navegamos al Fragment ResponsablesFragment
            navController.navigate(R.id.action_nav_usuario_to_nav_responsables);
        });
        return view;
    }

    // Método para obtener el nombre y los apellidos del usuario asociado a dni_usuario, enviando la solicitud al servidor
    public void consultar_usuario(){
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;
        //Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                //En caso de éxito mostramos los datos en pantalla
                                String nombre = response.getString("nombre");
                                String apellidos = response.getString("apellidos");
                                String fecha = response.getString("fecha_nacimiento");
                                textonombre.setText(nombre + " "+ apellidos);
                                textodni.setText(response.getString("dni"));
                                textotelefono.setText(response.getString("telefono"));
                                LocalDate fecha_nacimiento = LocalDate.parse(fecha);
                                LocalDate actual = LocalDate.now();
                                Period periodo = Period.between(fecha_nacimiento, actual);
                                int edad = periodo.getYears();
                                textoedad.setText(String.valueOf(edad) + " años (" + fecha + ")");
                                textodomicilio.setText(response.getString("domicilio"));
                                textoenfermedades.setText(response.getString("enfermedades_previas"));
                                textoalergias.setText(response.getString("alergias"));
                                textopassword.setText(response.getString("contrasena"));
                                usuario = new Usuario(nombre, apellidos, fecha_nacimiento, response.getString("domicilio"), response.getString("enfermedades_previas"), response.getString("alergias"), response.getString("dni"), Integer.valueOf(response.getString("telefono")), response.getString("contrasena"));
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

    // Método para editar un usuario, enviando la solicitud al servidor
    public void actualizar_usuario(Usuario usuario, Integer telefono, String domicilio, String enfermedades, String alergias, String password){

        // Creamos el JSON que vamos a enviar
        JSONObject new_usuario= new JSONObject();
        try {
            new_usuario.put("dni", usuario.getDni());
            new_usuario.put("nombre", usuario.getNombre());
            new_usuario.put("apellidos", usuario.getApellidos());
            new_usuario.put("telefono", telefono);
            new_usuario.put("fecha_nacimiento", usuario.getFecha_nacimiento());
            new_usuario.put("domicilio", domicilio);
            new_usuario.put("enfermedades_previas", enfermedades);
            new_usuario.put("alergias", alergias);
            new_usuario.put("contrasena",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"usuario/" + usuario.getDni();
        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_usuario,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Responsable actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment UsuarioFragment
                                // pasando el usuario editado en el bundle.
                                Usuario usuariofinal = new Usuario();
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(usuariofinal);
                                bundle.putString("jsonObject", json);
                                Log.d("UsuarioFragment", "JSON de usuarioMANDADO: " + json);
                                navController.popBackStack();
                                navController.navigate(R.id.nav_usuario, bundle);
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
                        "Se ha producido un error actualizando  el usuario", Toast.LENGTH_SHORT).show();
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }



}