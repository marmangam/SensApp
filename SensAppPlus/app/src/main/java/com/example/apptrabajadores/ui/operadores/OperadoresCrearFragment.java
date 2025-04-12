// Este archivo define el fragmento `OperadoresCrearFragment`.
// Permite a los administrativos crear un nuevo operador.

package com.example.apptrabajadores.ui.operadores;

// Imports
import android.app.Activity;
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
import com.example.apptrabajadores.Operador;
import com.example.apptrabajadores.R;
import com.example.apptrabajadores.ServiciosWeb;
import com.example.apptrabajadores.databinding.FragmentCrearOperadoresBinding;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperadoresCrearFragment extends Fragment {
    // Variables de la clase
    private FragmentCrearOperadoresBinding binding;
    Activity activity;
    Boolean activo = Boolean.FALSE;
    Boolean ocupado = Boolean.FALSE;
    EditText textonombre ;
    TextView textousuario;
    EditText textoapellidos;
    EditText textopassword;
    ImageView ver_contrasena;
    Button boton_crear;
    Operador operador;
    List<String> usuarios_operadores;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el layout de este fragmento
        binding = FragmentCrearOperadoresBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        // Obtenemos el NavController para la navegación entre fragmentos
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_administrativos);
        activity = getActivity();

        // Obtenemos una lista con todos los operadores para asegurarnos de no crear dos operadores con el mismo nombre de usuario
        usuarios_operadores = new ArrayList<>();
        listar_operadores();

        // Configuramos los datos del usuario en pantalla
        textonombre = binding.nombre;
        textoapellidos = binding.apellidos;
        textousuario = binding.usuario;
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

        // Configuramos el botón crear operador
        boton_crear = binding.botonCrearOperador;
        boton_crear.setOnClickListener(v ->{
            // Funcionamiento del botón:
            // Obtenemos los datos del operador a crear
            String nuevonombre = binding.nombre.getText().toString();
            String nuevoapellido = binding.apellidos.getText().toString();
            String nuevousuario = binding.usuario.getText().toString();
            String nuevapassword = binding.textPassword.getText().toString();
            // Comprobamos que no exista un operador con el nombre de usuario introducido
            Log.d("LISTA", "JSON del usuario: " + usuarios_operadores);
            if(usuarios_operadores.contains(nuevousuario) ){
                Log.d("PRUEBA", "JSON del usuario: " + "YA EXISTE EL USUARIO");
                Toast.makeText(activity.getApplicationContext(), "El usuario ya existe", Toast.LENGTH_SHORT).show();
            }// Comprobamos que no estén vacíos y actualizamos los datos si no lo están
            else if (nuevapassword.equals("") || nuevoapellido.equals("") || nuevonombre.equals("") || nuevousuario.equals("")) {
                Toast.makeText(activity.getApplicationContext(), "Rellene todos los campos!", Toast.LENGTH_SHORT).show();
            }else {
                crear_operador(operador, nuevonombre, nuevoapellido,nuevousuario, nuevapassword);
            }

        });


        return view;
    }


    // Método para crear un nuevo operador, enviando la solicitud al servidor
    public void crear_operador(Operador operador, String nombre, String apellidos,String usuario, String password){
        // Creamos el JSON que vamos a enviar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario", usuario);
            new_operador.put("nombre", nombre);
            new_operador.put("apellidos", apellidos);
            new_operador.put("contrasena",password);
            new_operador.put("activo", activo);
            new_operador.put("ocupado", ocupado);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"operador";

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_operador,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Insertado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Operador insertado", Toast.LENGTH_SHORT).show();
                                // En caso de que el operador se cree correctamente, se navega al fragment OperadoresEditarFragment
                                // pasando el operaador creado en el bundle
                                Operador operadorfinal = new Operador(usuario, nombre, apellidos, password, activo, ocupado);
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
                        "Se ha producido un error creando el operador", Toast.LENGTH_SHORT).show();
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


    // Método para obtener los nombres de usuario de todos los operadores existentes, enviando la solicitud al servidor
    public void listar_operadores(){
        final String URL = getString(R.string.IP) + "operador/lista";

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todos los nombres de usuarios de los operadores a una lista
                        try {
                            usuarios_operadores.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject operador = response.getJSONObject(i);
                                String usuario = operador.getString("usuario");
                                usuarios_operadores.add(usuario);
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