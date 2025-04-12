// Este archivo define la clase `MainResponsables`.
// Representa la actividad principal de la aplicación.

package com.example.appresponsables;

// Imports
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.appresponsables.databinding.ActivityResponsablesBinding;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;


public class MainResponsables extends AppCompatActivity {
    // Variables de la clase
    Intent intent;
    String stringjson;
    JSONObject ResponsableObject;
    String nombre_usuario;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityResponsablesBinding binding;

    public MainResponsables() throws JSONException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflamos el layout de este fragmento
        binding = ActivityResponsablesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configuramos la barra de herramientas
        setSupportActionBar(binding.appBarResponsables.toolbar);
        // Inicializamos la navegación
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Definimos los elementos de la barra de herramientas
        mAppBarConfiguration = new AppBarConfiguration.Builder( R.id.nav_datos,
                R.id.nav_eventos, R.id.nav_alarmas, R.id.nav_incidencias, R.id.nav_usuario)
                .setOpenableLayout(drawer)
                .build();
        // Inicializamos el controlador de navegación
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_responsables);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Obtenemos los datos del Intent (usuario del responsable que ha accedido a la aplicación)
        intent = getIntent();
        stringjson = intent.getStringExtra("jsonObject");
        try {
            ResponsableObject = new JSONObject(stringjson);
            nombre_usuario = ResponsableObject.getString("usuario");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Método para inflar el menú de opciones en la barra de herramientas
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_responsables, menu);
        return true;
    }

    // Método para manejar la navegación
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_responsables);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Método para manejar el cierre de sesión
    public void boton_cerrar(MenuItem item) {
        Intent intent = new Intent(MainResponsables.this, MainActivity.class);;
        startActivity(intent);
    }




}