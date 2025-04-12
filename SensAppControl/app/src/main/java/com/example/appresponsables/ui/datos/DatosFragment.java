// Este archivo define el fragmento `DatosFragment`.
// Permite a los usuarios responsables ver sus datos y modificar algunos de estos.
// Para esto se incluyen un método para obtener el nombre de su usuario haciendo mediante el dni_usuario
// y otro para actualizar los datos del usuario.

package com.example.appresponsables.ui.datos;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appresponsables.MainResponsables;
import com.example.appresponsables.R;
import com.example.appresponsables.Responsable;
import com.example.appresponsables.ServiciosWeb;
import com.example.appresponsables.databinding.FragmentDatosBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class DatosFragment extends Fragment {
    // Variables de la clase
    private FragmentDatosBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String dni_usuario;
    String nombre;
    String apellidos;
    String nombre_usuario;
    String dni;
    Integer telefono;
    String password;
    String nombre_final="";
    TextView textonombre ;
    TextView textousuario;
    EditText textotelefono;
    TextView textodni;
    TextView textonombre_usuario;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Responsable responsable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDatosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos la actividad y los datos del Intent (dni_usuario del responsable que ha accedido a la aplicación)
        activity = getActivity();
        if (activity != null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                nombre_usuario = UsuarioObject.getString("usuario");
                nombre = UsuarioObject.getString("nombre");
                apellidos = UsuarioObject.getString("apellidos");
                dni = UsuarioObject.getString("dni");
                dni_usuario = UsuarioObject.getString("dni_usuario");
                telefono = UsuarioObject.getInt("telefono");
                password = UsuarioObject.getString("contrasena");
                responsable = new Responsable(nombre_usuario, nombre, apellidos, dni, dni_usuario, telefono, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Configuramos los datos del usuario para mostrarlos en pantalla
        textonombre = binding.textNombre;
        textonombre.setText(textonombre.getText().toString() + nombre+ " " + apellidos + "!");
        textousuario = binding.textUsuario;
        textousuario.setText(textousuario.getText().toString() + nombre_usuario);
        textotelefono = binding.textTelefono;
        textotelefono.setText(String.valueOf(telefono));
        textodni = binding.textDni;
        textodni.setText(dni);
        textonombre_usuario = binding.textNombreUsuario;
        consultar_usuario();
        textopassword = binding.textPassword;
        textopassword.setText(password);
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
        boton_editar.setOnClickListener(v ->{
            // Funcionamiento del botón
            // Obtenemos los nuevos datos
            String nuevapassword = binding.textPassword.getText().toString();
            String textotelefono = binding.textTelefono.getText().toString();
            // Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            if(textotelefono.isEmpty() || nuevapassword.equals("")) {
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else {
                Integer nuevotelefono = Integer.valueOf(binding.textTelefono.getText().toString());
                actualizar_mis_datos(responsable, nuevotelefono,nuevapassword);
            }

        });

        return view;
    }

    // Método para obtener el nombre y los apellidos del usuario asociado a dni_usuario
    public void consultar_usuario(){
        final String URL = getString(R.string.IP) + "usuario/" + dni_usuario;
        //Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null) {
                                //En caso de éxito mostramos el nombre completo en pantalla
                                String nombre_del_usuario = response.getString("nombre");
                                String apellidos_usuario = response.getString("apellidos");
                                nombre_final = nombre_del_usuario + " " + apellidos_usuario;
                                textonombre_usuario.setText(nombre_final);
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

    // Método para actualizar los datos del responsable
    public void actualizar_mis_datos(Responsable responsable, Integer telefono, String password){

        // Creamos el JSON que vamos a mandar
        JSONObject new_responsable= new JSONObject();
        try {
            new_responsable.put("usuario", responsable.getUsuario());
            new_responsable.put("nombre", responsable.getNombre());
            new_responsable.put("apellidos", responsable.getApellidos());
            new_responsable.put("dni", responsable.getDni());
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
                                //Toast.makeText(activity.getApplicationContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment DatosFragment
                                // pasando el responsable en el Intent.
                                Intent intent = new Intent(getActivity(), MainResponsables.class);
                                intent.putExtra("jsonObject", new_responsable.toString());
                                startActivity(intent);
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
                Toast.makeText(activity.getApplicationContext(),"Se ha producido un error actualizando los datos", Toast.LENGTH_SHORT).show();
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