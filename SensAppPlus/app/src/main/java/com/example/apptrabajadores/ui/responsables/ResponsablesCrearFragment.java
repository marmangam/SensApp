// Este archivo define el fragmento `ResponsablesCrearFragment`.
// Permite a los administrativos crear un nuevo responsable para un usuario ya existente.


package com.example.apptrabajadores.ui.responsables;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentCrearResponsablesBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ResponsablesCrearFragment extends Fragment {
    // Variables de la clase
    private FragmentCrearResponsablesBinding binding;
    Activity activity;
    Intent intent;
    String dni_usuario;
    TextView textousuario;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_crear;
    List <String> usuarios_responsables;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentCrearResponsablesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_administrativos);
        // Obtenemos la actividad y los datos del bundle (dni_usuario del responsable  que se quiere crear)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity != null) {
            intent = activity.getIntent();
            if (bundle != null) {
                dni_usuario = bundle.getString("dni_usuario");
                Log.d("ResponsablesCrearFragment", "JSON recibido: " + dni_usuario);
            }
        }
        // Obtenemos una lista con todos los responables para asegurarnos de no crear dos responsables con el mismo nombre de usuario
        usuarios_responsables = new ArrayList<>();
        listar_usuarios();

        // Configuramos los datos del usuario en pantalla
        textousuario = binding.textUsuario;
        consultar_usuario(dni_usuario);
        textopassword = binding.textPassword;
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

        // Configuramos el botón crear responsable
        boton_crear = binding.botonCrearResponsable;
        boton_crear.setOnClickListener(v -> {
            // Funcionamiento del botón:
            // Obtenemos los datos del responsable a crear
            String nuevonombre = binding.textNombre.getText().toString();
            String nuevoapellido = binding.textApellidos.getText().toString();
            String nuevousuario = binding.textNombreUsuario.getText().toString();
            String nuevodni = binding.textDni.getText().toString();
            String textotelefono = binding.textTelefono.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            // Comprobamos que no exista un responsable con el nombre de usuario introducido
            if(usuarios_responsables.contains(nuevousuario)) {
                Toast.makeText(activity.getApplicationContext(), "Ya existe el usuario", Toast.LENGTH_SHORT).show();
            }// Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            else if(nuevonombre.equals("") || nuevoapellido.equals("") || nuevodni.equals("") || textotelefono.isEmpty() || nuevapassword.equals("") || nuevousuario.equals("")){
                    Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else{
                    Integer nuevotelefono = Integer.valueOf(binding.textTelefono.getText().toString());
                    crear_responsable(nuevonombre, nuevoapellido, nuevousuario, nuevotelefono, nuevodni, dni_usuario, nuevapassword);
            }

        });

        return root;
    }


    // Método para obtener los datos del  usuario asociado a dni_usuario
    public void consultar_usuario(String dni_usuario) {
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;

        //Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                String nombre_del_usuario = response.getString("nombre");
                                String apellidos_usuario = response.getString("apellidos");
                                textousuario.setText(nombre_del_usuario + " " + apellidos_usuario);

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


    // Método para crear un nuevo responsable, enviando la solicitud al servidor
    public void crear_responsable(String nombre, String apellidos, String usuario, Integer telefono, String dni, String dni_usuario, String password) {
        // Creamos el JSON que vamos a enviar
        JSONObject new_responsable = new JSONObject();
        try {
            new_responsable.put("usuario", usuario);
            new_responsable.put("nombre", nombre);
            new_responsable.put("apellidos", apellidos);
            new_responsable.put("dni", dni);
            new_responsable.put("dni_usuario", dni_usuario);
            new_responsable.put("telefono", telefono);
            new_responsable.put("contrasena", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP) + "responsable";

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_responsable,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Insertado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Responsable insertado", Toast.LENGTH_SHORT).show();
                                // En caso de que el responsable se cree correctamente, se navega al fragment ResponsablesEditarFragment
                                // pasando el responsable creado en el bundle
                                Bundle bundle = new Bundle();
                                bundle.putString("responsableObject", new_responsable.toString());
                                Log.d("UsuariosFragment", "JSON del usuario: " + new_responsable.toString());
                                navController.popBackStack();
                                navController.navigate(R.id.action_nav_responsables_to_nav_editar_responsables, bundle);


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
                        "Se ha producido un error creando al responsable", Toast.LENGTH_SHORT).show();
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
        binding = null;
    }

    // Método para obtener los nombres de usuario de todos los responsables existentes, enviando la solicitud al servidor
    public void listar_usuarios(){
        final String URL = getString(R.string.IP) + "responsable/lista";

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todos los nombres de usuarios de los responsables a una lista
                        try {
                            usuarios_responsables.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject responsable = response.getJSONObject(i);
                                String usuario = responsable.getString("usuario");
                                usuarios_responsables.add(usuario);
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
