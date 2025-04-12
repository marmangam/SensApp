// Este archivo define el fragmento `DatosAdministrativosFragment`.
// Permite a los administrativos ver sus datos y modificar algunos de estos.

package com.example.apptrabajadores.ui.datosAdministrativos;

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
import com.example.apptrabajadores.Administrativo;
import com.example.apptrabajadores.MainAdministrativos;
import com.example.apptrabajadores.databinding.FragmentDatosAdministrativosBinding;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class DatosAdministrativosFragment extends Fragment {
    // Variables de la clase
    private FragmentDatosAdministrativosBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String nombre;
    String apellidos;
    String nombre_usuario;
    String password;
    TextView textonombre;
    TextView textonombre_usuario;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Administrativo administrativo;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDatosAdministrativosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos la actividad y los datos del Intent (administrativo que ha accedido a la aplicación)
        activity = getActivity();
        if (activity != null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                nombre_usuario = UsuarioObject.getString("usuario");
                nombre = UsuarioObject.getString("nombre");
                apellidos = UsuarioObject.getString("apellidos");
                password = UsuarioObject.getString("contrasena");
                administrativo = new Administrativo(nombre_usuario, nombre, apellidos, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Configuramos los datos del administrativo para mostrarlos en pantalla
        textonombre =binding.textNombre;
        textonombre.setText(textonombre.getText().toString() + nombre + " "+ apellidos + "!");
        textonombre_usuario = binding.textUsuario;
        textonombre_usuario.setText(textonombre_usuario.getText().toString()+ nombre_usuario);
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
            // Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            if (!nuevapassword.equals("")) {
                actualizar_mis_datos(administrativo, nuevapassword);
            }

        });


        return root;
    }


    // Método para actualizar los datos del administrativo
    public void actualizar_mis_datos(Administrativo administrativo, String password){

        // Creamos el JSON que vamos a mandar
        JSONObject new_administrativo= new JSONObject();
        try {
            new_administrativo.put("usuario", administrativo.getUsuario());
            new_administrativo.put("nombre", administrativo.getNombre());
            new_administrativo.put("apellidos", administrativo.getApellidos());
            new_administrativo.put("contrasena",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"administrativo/" + administrativo.getUsuario();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_administrativo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Datos actualizados", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment DatosAdministrativosFragment
                                // pasando el administrativo en el Intent.
                                Intent intent = new Intent(getActivity(), MainAdministrativos.class);
                                intent.putExtra("jsonObject", new_administrativo.toString());
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