// Este archivo define el fragmento `OperadoresEditarFragment`.
// Permite a los administrativos editar los datos de un operador.
// Para esto se incluyen un método para actualizar los datos.

package com.example.apptrabajadores.ui.operadores;

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
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.Operador;
import com.example.apptrabajadores.databinding.FragmentEditarOperadoresBinding;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class OperadoresEditarFragment extends Fragment {
    // Variables de la clase
    private FragmentEditarOperadoresBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject OperadorObject;
    String nombre;
    String apellidos;
    String usuario;
    String password;
    Boolean activo;
    Boolean ocupado;
    EditText textonombre ;
    TextView textousuario;
    EditText textoapellidos;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_editar;
    Operador operador;

    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentEditarOperadoresBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_administrativos);
        // Obtenemos la actividad y los datos del bundle (operador que se quiere editar)
        activity = getActivity();
        Bundle bundle = getArguments();
        if (activity != null) {
            intent = activity.getIntent();
            if (bundle != null) {
                stringjson = bundle.getString("operadorObject");
                try {
                    OperadorObject = new JSONObject(stringjson);
                    usuario = OperadorObject.getString("usuario");
                    nombre = OperadorObject.getString("nombre");
                    apellidos = OperadorObject.getString("apellidos");
                    password = OperadorObject.getString("contrasena");
                    activo = OperadorObject.getBoolean("activo");
                    ocupado = OperadorObject.getBoolean("ocupado");
                    operador = new Operador(usuario, nombre, apellidos, password, activo, ocupado);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // Configuramos los datos del operador en pantalla
        textonombre = binding.nombre;
        textonombre.setText(nombre);
        textoapellidos = binding.apellidos;
        textoapellidos.setText(apellidos);
        textousuario = binding.usuario;
        textousuario.setText(usuario);
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
        boton_editar = binding.botonEditarOperador;
        boton_editar.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los nuevos datos
            String nuevonombre = binding.nombre.getText().toString();
            String nuevoapellido = binding.apellidos.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            // Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            if (!nuevapassword.equals("") && !nuevoapellido.equals("") && !nuevonombre.equals("")){
                actualizar_operador(operador, nuevonombre, nuevoapellido, nuevapassword);
            }else {
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }

            });


        return view;
    }


    // Método para editar un operador, enviando la solicitud al servidor
    public void actualizar_operador(Operador operador, String nombre, String apellidos, String password){

        // Creamos el JSON que vamos a enviar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario", operador.getUsuario());
            new_operador.put("nombre", nombre);
            new_operador.put("apellidos", apellidos);
            new_operador.put("contrasena",password);
            new_operador.put("activo", operador.getActivo());
            new_operador.put("ocupado", operador.getOcupado());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"operador/" + operador.getUsuario();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_operador,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Operador actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se navega al fragment OperadoresEditarFragment
                                // pasando el operador editado en el bundle
                                Operador operadorfinal = new Operador(operador.getUsuario(), nombre, apellidos, password, operador.getActivo(), operador.getOcupado());
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(operadorfinal);
                                bundle.putString("operadorObject", json);
                                Log.d("OperadoresFragment", "JSON del usuario: " + json);
                                navController.popBackStack();
                                navController.navigate(R.id.action_nav_operadores_to_nav_editar_operadores, bundle);
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

    }





}