// Clase utilizada para gestionar una única instancia de RequestQueue de la biblioteca Volley
// para realizar solicitudes de red en la aplicación.
// Implementa el patrón Singleton para asegurar que solo exista una instancia de RequestQueue en toda la aplicación.

package com.example.appusuarios;

// Imports
import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class ServiciosWeb extends Application {
    private static ServiciosWeb sInstance;
    private RequestQueue mRequestQueue;
    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);
        sInstance = this;
    }
    public synchronized static ServiciosWeb
    getInstance() {
        return sInstance;
    }
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}