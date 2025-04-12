// Este archivo define la clase `MainAdministrativos`.
// Representa la actividad principal de la parte de la aplicación dedicada a los administrativos.

package com.example.apptrabajadores;

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
import com.example.apptrabajadores.databinding.ActivityAdministrativosBinding;
import com.google.android.material.navigation.NavigationView;

public class MainAdministrativos extends AppCompatActivity {
    // Variables de la clase
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAdministrativosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflamos el layout de este fragmento
        binding = ActivityAdministrativosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configuramos la barra de herramientas
        setSupportActionBar(binding.appBarAdministrativos.toolbar);
        // Inicializamos la navegación
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Definimos los elementos de la barra de herramientas
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_datos_administrativos, R.id.nav_usuarios, R.id.nav_crear_usuarios, R.id.nav_operadores, R.id.nav_incidencias)
                .setOpenableLayout(drawer)
                .build();
        // Inicializamos el controlador de navegación
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_administrativos);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    // Método para inflar el menú de opciones en la barra de herramientas
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_administrativos, menu);
        return true;
    }

    // Método para manejar la navegación
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_administrativos);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Método para manejar el cierre de sesión
    public void boton_cerrar(MenuItem item) {
        Intent intent = new Intent(MainAdministrativos.this, MainActivity.class);;
        startActivity(intent);
    }
}