// Este archivo define el fragmento `ResponsablesEditarFragment`.
// Permite a los administrativos editar los datos de un responsable.
// Para esto se incluyen un método para actualizar los datos.

package com.example.apptrabajadores.ui.responsables;

// Imports
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.content.Intent;
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
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.Responsable;
import com.android.volley.Response;
import com.example.apptrabajadores.databinding.FragmentEditarResponsablesBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ResponsablesEditarFragment extends Fragment {
    // Variables de la clase
    private FragmentEditarResponsablesBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject ResponsableObject;
    String nombre;
    String nombre_usuario;
    String apellidos;
    String dni;
    Integer telefono;
    String dni_usuario;
    String password;
    EditText textonombre;
    EditText textoapellidos;
    TextView textonombre_usuario;
    EditText textotelefono;
    EditText textodni;
    TextView textousuario;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Responsable responsable;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentEditarResponsablesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_administrativos);
        // Obtenemos la actividad y los datos del bundle (responsable que se quiere editar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity != null) {
            intent = activity.getIntent();
            if (bundle != null) {
                stringjson = bundle.getString("responsableObject");

                Log.d("ResponsablesEditarFragment", "JSON recibido: " + stringjson);
                try {
                    ResponsableObject = new JSONObject(stringjson);
                    nombre_usuario = ResponsableObject.getString("usuario");
                    nombre = ResponsableObject.getString("nombre");
                    apellidos = ResponsableObject.getString("apellidos");
                    dni = ResponsableObject.getString("dni");
                    dni_usuario = ResponsableObject.getString("dni_usuario");
                    telefono = ResponsableObject.getInt("telefono");
                    password = ResponsableObject.getString("contrasena");
                    responsable = new Responsable(nombre_usuario, nombre, apellidos, dni, dni_usuario, telefono, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // Configuramos los datos del responsable en pantalla
        textonombre = binding.textNombre;
        textonombre.setText(responsable.getNombre());
        textoapellidos = binding.textApellidos;
        textoapellidos.setText(responsable.getApellidos());
        textonombre_usuario = binding.textNombreUsuario;
        textonombre_usuario.setText(textonombre_usuario.getText().toString() + responsable.getUsuario());
        textotelefono = binding.textTelefono;
        textotelefono.setText(String.valueOf(responsable.getTelefono()));
        textodni = binding.textDni;
        textodni.setText(responsable.getDni());
        textousuario = binding.textUsuario;
        consultar_usuario(responsable.getDni_usuario());
        textopassword = binding.textPassword;
        textopassword.setText(responsable.getContrasena());
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
        boton_editar = binding.botonEditarDatos;
        boton_editar.setOnClickListener(v -> {
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos
            String nuevonombre = binding.textNombre.getText().toString();
            String nuevoapellido = binding.textApellidos.getText().toString();
            String nuevodni = binding.textDni.getText().toString();
            String textotelefono = binding.textTelefono.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            // Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            if(nuevonombre.equals("") || nuevoapellido.equals("") || nuevodni.equals("") || textotelefono.isEmpty() || nuevapassword.equals("")){
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else{
                Integer nuevotelefono = Integer.valueOf(binding.textTelefono.getText().toString());
                actualizar_datos(responsable, nuevonombre, nuevoapellido, nuevotelefono, nuevodni, nuevapassword);
            }

        });

        return root;
    }


    // Método para obtener el nombre y los apellidos del usuario asociado a dni_usuario, enviando la solicitud al servidor
    public void consultar_usuario(String dni_usuario){
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;
        //Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                //En caso de éxito mostramos los datos en pantalla
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



    // Método para editar un responsable, enviando la solicitud al servidor
    public void actualizar_datos(Responsable responsable, String nombre, String apellidos, Integer telefono, String dni, String password){

        // Creamos el JSON que vamos a enviar
        JSONObject new_responsable= new JSONObject();
        try {
            new_responsable.put("usuario", responsable.getUsuario());
            new_responsable.put("nombre", nombre);
            new_responsable.put("apellidos", apellidos);
            new_responsable.put("dni", dni);
            new_responsable.put("dni_usuario", responsable.getDni_usuario());
            new_responsable.put("telefono", telefono);
            new_responsable.put("contrasena",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"responsable/" + responsable.getUsuario();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_responsable,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Responsable actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment ResponsablesEditarFragment
                                // pasando el responsable editado en el bundle
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
                        "Se ha producido un error actualizando los datos", Toast.LENGTH_SHORT).show();
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
}