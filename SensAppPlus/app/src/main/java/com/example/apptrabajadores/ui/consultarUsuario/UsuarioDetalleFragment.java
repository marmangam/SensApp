// Este archivo define el fragmento `UsuarioDetalleFragment`.
// Permite a los operadores ver y editar los datos de un usuario concreto
// Para esto se incluyen un método para actualizar los datos.
// También permite navegar al fragmento responsable de mostrar una lista con todos los responsables del usuario.

package com.example.apptrabajadores.ui.consultarUsuario;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.apptrabajadores.Usuario;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentDetalleUsuarioBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

public class UsuarioDetalleFragment extends Fragment {
    // Variables de la clase
    private FragmentDetalleUsuarioBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    TextView textonombre;
    TextView textodni;
    TextView textotelefono;
    TextView textoedad;
    TextView textodomicilio;
    EditText textoenfermedades;
    EditText textoalergias;
    Button boton_editar;
    Button ver_responsables;
    Usuario usuario;
    String dni;
    String nombre;
    String apellidos;
    Integer telefono;
    String f_nac;
    LocalDate fecha;
    String domicilio;
    String enfermedades;
    String alergias;
    String password;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDetalleUsuarioBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_operadores);
        // Obtenemos la actividad y los datos del bundle (usuario que se quiere actualizar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity != null) {
            intent = activity.getIntent();
            if (bundle != null) {
                stringjson = bundle.getString("usuarioObject");

                Log.d("UsuariosEditarFragment", "JSON recibido: " + stringjson);
                try {
                    UsuarioObject = new JSONObject(stringjson);
                    dni = UsuarioObject.getString("dni");
                    nombre = UsuarioObject.getString("nombre");
                    apellidos = UsuarioObject.getString("apellidos");
                    telefono = UsuarioObject.getInt("telefono");
                    f_nac = UsuarioObject.getString("fecha_nacimiento");
                    fecha = LocalDate.parse(f_nac);
                    domicilio = UsuarioObject.getString("domicilio");
                    enfermedades = UsuarioObject.getString("enfermedades_previas");
                    alergias = UsuarioObject.getString("alergias");
                    password = UsuarioObject.getString("contrasena");
                    usuario = new Usuario(nombre, apellidos, fecha, domicilio, enfermedades, alergias, dni, telefono, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        // Configuramos los datos del usuario en pantalla
        textonombre = binding.textNombre;
        textonombre.setText(nombre + " "+ apellidos);
        textodni = binding.textDni;
        textodni.setText(dni);
        textotelefono = binding.textTelefono;
        textotelefono.setText(String.valueOf(telefono));
        textoedad = binding.textEdad;
        LocalDate actual = LocalDate.now();
        Period periodo = Period.between(fecha, actual);
        int edad = periodo.getYears();
        textoedad.setText(String.valueOf(edad) + " años (" + f_nac + ")");
        textodomicilio = binding.textdomicilio;
        textodomicilio.setText(domicilio);
        textoenfermedades = binding.textenfermedades;
        textoenfermedades.setText(enfermedades);
        textoalergias = binding.textalergias;
        textoalergias.setText(alergias);

        // Configuramos el botón editar datos
        boton_editar = binding.botonEditarUsuario;
        boton_editar.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos
            String nuevaenfermedad = binding.textenfermedades.getText().toString();
            String nuevaalergia= binding.textalergias.getText().toString();
            // Actualizamos los datos
            actualizar_usuario(usuario, nuevaenfermedad, nuevaalergia);

        });

        // Configuramos el botón ver responsables
        ver_responsables = binding.botonVerResponsables;
        ver_responsables.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Añadimos al bundle el dni del usuario y navegamos al fragmento ResponsablesListaFragment
            Bundle bundle2 = new Bundle();
            bundle2.putString("dni_usuario", usuario.getDni());
            Log.d("UsuariosEditarFragment", "dni del usuario: " + usuario.getDni());
            navController.navigate(R.id.action_nav_detalle_usuario_to_nav_lista_responsables, bundle2);
        });


        return view;
    }


    // Método para editar un usuario, enviando la solicitud al servidor
    public void actualizar_usuario(Usuario usuario, String enfermedades, String alergias){

        // Creamos el JSON que vamos a enviar
        JSONObject new_usuario= new JSONObject();
        try {
            new_usuario.put("dni", usuario.getDni());
            new_usuario.put("nombre", usuario.getNombre());
            new_usuario.put("apellidos", usuario.getApellidos());
            new_usuario.put("telefono", usuario.getTelefono());
            new_usuario.put("fecha_nacimiento", usuario.getFecha_nacimiento());
            new_usuario.put("domicilio", usuario.getDomicilio());
            new_usuario.put("enfermedades_previas", enfermedades);
            new_usuario.put("alergias", alergias);
            new_usuario.put("contrasena",usuario.getContrasena());
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
                                // Toast.makeText(activity.getApplicationContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment UsuarioDetalleFragment
                                // pasando el usuario editado en el bundle
                                Usuario usuariofinal = new Usuario(usuario.getNombre(), usuario.getApellidos(), usuario.getFecha_nacimiento(), usuario.getDomicilio(), enfermedades, alergias, usuario.getDni(), usuario.getTelefono(), usuario.getContrasena());
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(usuariofinal);
                                bundle.putString("usuarioObject", json);
                                Log.d("UsuariosFragment", "JSON del usuario: " + json);
                                navController.popBackStack();
                                navController.navigate(R.id.action_nav_consultar_usuario_to_nav_detalle_usuario, bundle);

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
                Toast.makeText(activity.getApplicationContext(), "Se ha producido un error actualizando los datos", Toast.LENGTH_SHORT).show();
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