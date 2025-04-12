// Este archivo define el fragmento `DatosOperadoresFragment`.
// Permite a los operadores ver sus datos y modificar algunos de estos.

package com.example.apptrabajadores.ui.datosOperadores;

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
import com.example.apptrabajadores.Operador;
import com.example.apptrabajadores.MainOperadores;
import com.example.apptrabajadores.databinding.FragmentDatosOperadoresBinding;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class DatosOperadoresFragment extends Fragment {
    // Variables de la clase
    private FragmentDatosOperadoresBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String nombre;
    String apellidos;
    String nombre_usuario;
    String password;
    Boolean activo;
    Boolean ocupado;
    TextView textonombre;
    TextView textonombre_usuario;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Operador operador;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentDatosOperadoresBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Obtenemos la actividad y los datos del Intent (operador que ha accedido a la aplicación)
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
                activo = UsuarioObject.getBoolean("activo");
                ocupado = UsuarioObject.getBoolean("ocupado");
                operador = new Operador(nombre_usuario, nombre, apellidos, password, activo, ocupado);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Configuramos los datos del operador para mostrarlos en pantalla
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
                actualizar_mis_datos(operador, nuevapassword);
            }

        });

        return root;
    }


    // Método para actualizar los datos del operador
    public void actualizar_mis_datos(Operador operador, String password){

        // Creamos el JSON que vamos a mandar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario", operador.getUsuario());
            new_operador.put("nombre", operador.getNombre());
            new_operador.put("apellidos", operador.getApellidos());
            new_operador.put("contrasena",password);
            new_operador.put("activo",operador.getActivo());
            new_operador.put("ocupado", operador.getOcupado());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"operador/" + operador.getUsuario();

        // Mandamos la solcitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_operador,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "responsable actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment DatosOperadoresFragment
                                // pasando el operador en el Intent.
                                Intent intent = new Intent(getActivity(), MainOperadores.class);
                                intent.putExtra("jsonObject", new_operador.toString());
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
        binding = null;
    }
}