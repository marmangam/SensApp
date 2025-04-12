// Este archivo define la clase `MainActivity`.
// Representa la actividad  de login de la aplicación.

package com.example.appusuarios;

// Imports
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    // Variables de la clase
    private EditText textodni;
    private EditText textopassword;
    private ArrayList<Usuario> guardados = new ArrayList<>();
    private Button boton_acceso;

    // Método que se ejecuta al crear la actividad para establecer el contenido de la vista con el layout activity_main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CREATE", "método onCreate de la aplicación");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Se intenta iniciar sesión con los datos de la bbdd local, para evitar el inicio de sesión manual
        peticion_acceso();

        // En caso de que no se pueda iniciar sesión con los datos de la bbdd se utilizaría el método de inicio de
        // sesión mediante un botón
        boton_acceso = findViewById(R.id.button_acceso);
        boton_acceso.setOnClickListener(v ->{
            textodni = (EditText) findViewById(R.id.txtDni);
            textopassword = (EditText) findViewById(R.id.txtPassword);
            consulta_acceso(textodni.getText().toString(), textopassword.getText().toString());

        });
    }

    // Método obtener los datos de inicio de sesión de la bbdd local
    public void peticion_acceso (){
        // Se obtienen los datos guardados en la bbdd local y si se encuentra algún usuario
        textodni = (EditText) findViewById(R.id.txtDni);
        textopassword = (EditText) findViewById(R.id.txtPassword);
        BaseDatos MBD = new BaseDatos(this);
        ArrayList<Usuario> lista_usuarios = MBD.recuperarUSUARIOS();
        guardados.clear();
        if(lista_usuarios !=null && !lista_usuarios.isEmpty()){
            for(Usuario usuario: lista_usuarios){
                guardados.add(usuario);
            }
            if (!guardados.isEmpty()) {
                // Si hay algún usuario guardado se manda la petición al servidor
                consulta_acceso(guardados.get(0).getDni(), guardados.get(0).getContrasena());

            }
        }

    }



    // Método para iniciar sesión en la aplicación, enviando la solicitud al servidor
    public void consulta_acceso (String dni, String contrasena){
        final String URL = getString(R.string.IP) + "usuario?dni=" + dni+ "&contrasena=" + contrasena;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response!=null){
                                String username=response.getString("dni");
                                String password = response.getString("contrasena");
                                if(username.equals(dni) && password.equals(contrasena)) {
                                    // En caso de que se encuentre el usuario se inicia la actividad MainUsuarios
                                    // pasando el usuario en el intent, el dni en las preferencias y además se guardan
                                    // los datos de inicio de sesión en la bbdd local de la aplicación
                                    //Toast.makeText(getApplicationContext(),"Usuario encontrado", Toast.LENGTH_SHORT).show();
                                    BaseDatos MBD = new BaseDatos(MainActivity.this);
                                    Boolean insertado = MBD.insertarUSUARIO(dni, contrasena);
                                    if (insertado) {
                                      //Toast.makeText(getApplicationContext(), "Usuario guardado", Toast.LENGTH_SHORT).show();
                                    }
                                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("dni", dni);
                                    editor.apply();
                                    Intent intent = new Intent(MainActivity.this, MainUsuarios.class);
                                    intent.putExtra("jsonObject", response.toString());
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                // En caso de error se imprime un Toast por pantalla para informar al usuario
                                Toast.makeText(getApplicationContext(), "Los datos introducidos no son correctos", Toast.LENGTH_SHORT).show();
                            } } catch (JSONException e){
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