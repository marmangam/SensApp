// Este archivo define la clase `MainOperadores`.
// Representa la actividad principal de la parte de la aplicación dedicada a los operadores.

package com.example.apptrabajadores;

// Imports
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.apptrabajadores.databinding.ActivityOperadoresBinding;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainOperadores extends AppCompatActivity {
    // Variables de la clase
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityOperadoresBinding binding;
    Activity activity;
    Intent intent;
    String stringjson;
    JSONObject UsuarioObject;
    String operador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflamos el layout de este fragmento
        binding = ActivityOperadoresBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtenemos los datos del Intent (usuario del operador que ha accedido a la aplicación)
        activity = this;
        if (activity != null) {
            intent = activity.getIntent();
            stringjson = intent.getStringExtra("jsonObject");
            try {
                UsuarioObject = new JSONObject(stringjson);
                operador = UsuarioObject.getString("usuario");
                Log.d("MainOperadores", "JSON de operador: " + operador);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Configuramos la barra de herramientas
        setSupportActionBar(binding.appBarOperadors.toolbar);
        // Inicializamos la navegación
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Definimos los elementos de la barra de herramientas
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_datos_operadores, R.id.nav_mis_incidencias, R.id.nav_consultar_usuario, R.id.nav_atender)
                .setOpenableLayout(drawer)
                .build();
        // Inicializamos el controlador de navegación
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_operadores);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    // Método para inflar el menú de opciones en la barra de herramientas
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_operadores, menu);
        return true;
    }

    // Método para manejar la navegación
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_operadores);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Método para manejar el cierre de sesión; se actualiza el operador para marcarlo como NO ACTIVO
    public void boton_cerrar(MenuItem item) {
        actualizar_operador();
        Intent intent = new Intent(MainOperadores.this, MainActivity.class);;
        startActivity(intent);
    }

    // Método para actualizar un operador, enviando la solicitud al servidor
    public void actualizar_operador(){
        // Creamos el JSON que vamos a mandar
            JSONObject new_operador= new JSONObject();
            try {
                new_operador.put("usuario", operador);
                new_operador.put("nombre", UsuarioObject.getString("nombre"));
                new_operador.put("apellidos", UsuarioObject.getString("apellidos"));
                new_operador.put("contrasena",UsuarioObject.getString("contrasena"));
                new_operador.put("activo", Boolean.FALSE);
                new_operador.put("ocupado", UsuarioObject.getBoolean("ocupado"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String URL = getString(R.string.IP)  +"operador/" + operador;

        // Mandamos la solcitud
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_operador,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("message").equals("Actualizado")) {
                                    //Toast.makeText(activity.getApplicationContext(), "Operador actualizado", Toast.LENGTH_SHORT).show();
                                    // Los datos se han actualizado correctamente
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
                            "Se ha producido un error actualizando el operador", Toast.LENGTH_SHORT).show();
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
}