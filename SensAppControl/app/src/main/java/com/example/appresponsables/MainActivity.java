// Este archivo define la clase `MainActivity`.
// Representa la actividad  de login de la aplicación.

package com.example.appresponsables;

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
import android.content.Intent;



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

    // Método para iniciar sesión en la aplicación, enviando la solicitud al servidor
    public void peticion_acceso (View view){
        txt_usuario = (EditText) findViewById(R.id.txtUsuario);
        txt_password = (EditText) findViewById(R.id.txtPassword);
        final String URL = getString(R.string.IP) + "responsable?usuario=" + txt_usuario.getText().toString()+ "&contrasena=" + txt_password.getText().toString();

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response!=null){
                                String username=response.getString("usuario");
                                if(username.equals(txt_usuario.getText().toString())) {
                                    //Toast.makeText(getApplicationContext(),   "Usuario encontrado", Toast.LENGTH_SHORT).show();
                                    // En caso de que se encuentre el usuario se inicia la actividad MainResponsables
                                    // pasando el usuario en el intent.
                                    Intent intent = new Intent(MainActivity.this, MainResponsables.class);
                                    intent.putExtra("jsonObject", response.toString());
                                    startActivity(intent);
                                }
                            }  } catch (JSONException e){
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(),"Los datos introducidos no son correctos", Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        });
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