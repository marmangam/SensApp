// Este archivo define la clase `MainActivity`.
// Representa la actividad  de login de la aplicación.

package com.example.apptrabajadores;

// Imports
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.*;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    // Variables de la clase
    private EditText txt_usuario;
    private EditText txt_password;

    // Método que se ejecuta al crear la actividad para establecer el contenido de la vista con el layout activity_main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CREATE", "método onCreate de la aplicación");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Método para iniciar sesión en la aplicación como administrativo, enviando la solicitud al servidor
    public void peticion_acceso_administrativo (View view){
        txt_usuario = (EditText) findViewById(R.id.txtUsuario);
        txt_password = (EditText) findViewById(R.id.txtPassword);
        final String URL = getString(R.string.IP) + "administrativo?usuario=" + txt_usuario.getText().toString()+ "&contrasena=" + txt_password.getText().toString();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response!=null){
                                String username=response.getString("usuario");
                                if(username.equals(txt_usuario.getText().toString())) {
                                    //Toast.makeText(getApplicationContext(), "Usuario encontrado", Toast.LENGTH_SHORT).show();
                                    // En caso de que se encuentre el usuario se inicia la actividad MainAdministrativos
                                    // pasando el usuario en el intent.
                                    Intent intent = new Intent(MainActivity.this, MainAdministrativos.class);
                                    intent.putExtra("jsonObject", response.toString());
                                    startActivity(intent);
                                }
                            } } catch (JSONException e){
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de que no se encuentre el usuario  como administrativo se prueba el acceso como operador
                peticion_acceso_operador(view);

            }
        });
        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }


    // Método para iniciar sesión en la aplicación como operador, enviando la solicitud al servidor
    public void peticion_acceso_operador (View view){
        txt_usuario = (EditText) findViewById(R.id.txtUsuario);
        txt_password = (EditText) findViewById(R.id.txtPassword);
        final String URL = getString(R.string.IP) + "operador?usuario=" + txt_usuario.getText().toString()+ "&contrasena=" + txt_password.getText().toString();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response!=null){
                                String username=response.getString("usuario");
                                if(username.equals(txt_usuario.getText().toString())) {
                                    // Toast.makeText(getApplicationContext(),"Usuario encontrado",Toast.LENGTH_SHORT).show();
                                    // En caso de que se encuentre el usuario:
                                    // Se actualiza el operador a ACTIVO
                                    actualizar_operador(username, response.getString("nombre"), response.getString("apellidos"), response.getString("contrasena"), response.getBoolean("ocupado"));
                                    // y se inicia la actividad MainOperadores pasando el usuario en el intent.
                                    Intent intent = new Intent(MainActivity.this, MainOperadores.class);
                                    intent.putExtra("jsonObject", response.toString());
                                    startActivity(intent);
                                }
                            }} catch (JSONException e){
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(), "Los datos introducidos no son correctos", Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        });
        ServiciosWeb.getInstance().getRequestQueue().add(request);
    }

    // Método para actualizar un operador, enviando la solicitud al servidor
    public void actualizar_operador(String usuario,String nombre, String apellidos, String password, Boolean ocupado){

        // Creamos el JSON que vamos a mandar
        JSONObject new_operador= new JSONObject();
        try {
            new_operador.put("usuario",  usuario);
            new_operador.put("nombre", nombre);
            new_operador.put("apellidos", apellidos);
            new_operador.put("contrasena",password);

            new_operador.put("activo", Boolean.TRUE);
            new_operador.put("ocupado", ocupado);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"operador/" + usuario;

        // Mandamos la solcitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_operador,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(activity.getApplicationContext(), "Operador actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que los datos se actualicen correctamente, se pasa el operador actualizado en el bundle de la actividad
                                Operador operadorfinal = new Operador(usuario, nombre, apellidos, password, new_operador.getBoolean("activo"), ocupado);
                                Bundle bundle = new Bundle();
                                Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
                                String json = gson.toJson(operadorfinal);
                                bundle.putString("operadorObject", json);
                                Log.d("OperadoresFragment", "JSON del usuario: " + json);
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
                Toast.makeText(getApplicationContext(), "Se ha producido un error actualizando el operador", Toast.LENGTH_SHORT).show();

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
    protected void onStop() {
        Log.d("STOP","método onStop de la aplicación");
        super.onStop();
    }
    @Override
    protected void onPause() {
        Log.d("PAUSE","método onPause de la aplicación");
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        Log.d("DESTROY","método onDestroy de la aplicación");
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        Log.d("RESUME","método onResume de la aplicación");
        super.onResume();
    }

}