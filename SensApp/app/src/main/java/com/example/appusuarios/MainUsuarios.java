// Este archivo define la clase `MainUsuarios`.
// Representa la actividad  principal de la aplicación.
// Se muestran dos listas, una con los eventos del dia y otra con las alarmas.
// Además se cuenta con la funcionalidad de crear incidencias.

package com.example.appusuarios;

// Imports
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.appusuarios.utilidades.MarginItemDecoration;
import com.example.appusuarios.utilidades.MarginItemDecorationHorizontal;
import android.app.PendingIntent;
import android.app.AlarmManager;
import java.util.Calendar;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;


public class MainUsuarios extends AppCompatActivity implements SensorEventListener {
    // Variables de la clase
    private SensorManager sensorManager;
    private Sensor acelerometro;
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.65F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTime=0;
    private ListaAdaptadorAlarmas adaptadorAlarmas;
    private List<Alarma> alarmas;
    private ListaAdaptadorEventos adaptadorEventos;
    private List<Evento> eventos;
    String dni;
    private MediaPlayer mediaPlayer;
    private ImageView imagenIncidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se configura el layout de esta actividad
        setContentView(R.layout.activity_usuarios);

        // Se establece el dni del usuario en las preferencias de la actividad
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        dni = sharedPreferences.getString("dni", null);

        // Configuramos la vista para mostrar las alarmas en una matriz de 2 columnas
        RecyclerView recyclerViewAlarma = findViewById(R.id.recycler_view_alarmas);
        recyclerViewAlarma.setLayoutManager(new GridLayoutManager(this, 2));
        int margen = getResources().getDimensionPixelSize(R.dimen.recycler_view_alarma_margen);
        recyclerViewAlarma.addItemDecoration(new MarginItemDecoration(margen));
        alarmas = new ArrayList<>();
        adaptadorAlarmas = new ListaAdaptadorAlarmas(alarmas, alarma -> {
            alarma.setCompletado(LocalDate.now());
            int hora = alarma.getHora().getHour();
            int minutos = alarma.getHora().getMinute();
            String txhora = String.format("%02d:%02d:00", hora, minutos);
            Log.d("AlarmasFragment", "STRING DE la hora: " + txhora);
            actualizar_alarma(alarma, txhora);
        });
        recyclerViewAlarma.setAdapter(adaptadorAlarmas);


        mostrar_alarmas();

        // Configuramos la vista para mostrar los eventos en una matriz de 1 columna
        RecyclerView recyclerViewEventos = findViewById(R.id.recycler_view_eventos);
        recyclerViewEventos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        int margen2 = getResources().getDimensionPixelSize(R.dimen.recycler_view_evento_margen);
        recyclerViewEventos.addItemDecoration(new MarginItemDecorationHorizontal(margen2));
        eventos = new ArrayList<>();
        adaptadorEventos = new ListaAdaptadorEventos(eventos, evento -> {
            evento.setCompletado(true);
            actualizar_evento(evento);
        });
        recyclerViewEventos.setAdapter(adaptadorEventos);
        // Mostramos todos los eventos
        mostrar_eventos();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        imagenIncidencia = findViewById(R.id.imagen_incidencia);
        imagenIncidencia.setOnLongClickListener(v ->{
            confirmar_incidencia(dni);
            return true;

        });


        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("showDialog")) {
            String nombre = getIntent().getStringExtra("nombre");
            String hora = getIntent().getStringExtra("hora");
            showAlarmDialog(nombre, hora);
        }
    }



    // Método para obtener todas las alarmas de un dni_usuario, enviando la solicitud al servidor
    public void mostrar_alarmas(){
        final String URL = getString(R.string.IP) + "alarma/lista?dni_usuario=" + dni;

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden todas las alarmas recibidas a una lista
                        try {
                            alarmas.clear();
                            for (int i =0; i<response.length(); i++){
                                JSONObject alarma = response.getJSONObject(i);
                                String dni_usuario = alarma.getString("dni_usuario");
                                String nombre = alarma.getString("nombre");
                                String tipo = alarma.getString("tipo");
                                String s_hora = alarma.getString("hora");
                                LocalTime hora = LocalTime.parse(s_hora);
                                LocalDate completado = LocalDate.of(2020, 1, 1);
                                if (!alarma.isNull("completado")) {
                                    String s_completado = alarma.getString("completado");
                                    completado = LocalDate.parse(s_completado);
                                }
                                alarmas.add(new Alarma(dni_usuario, nombre, tipo, hora, completado));
                            }
                            // Se ordenan por horas y por si se han completado hoy o no
                            alarmas.sort((a1, a2) -> {
                                LocalDate fecha_actua = LocalDate.now();

                                // Comprobar si ambas alarmas se han completadas hoy
                                boolean a1CompletadoHoy = a1.getCompletado() != null && a1.getCompletado().equals(fecha_actua);
                                boolean a2CompletadoHoy = a2.getCompletado() != null && a2.getCompletado().equals(fecha_actua);

                                // Si ambas alarmas se han completadas hoy, se ordenan por hora
                                if (a1CompletadoHoy && a2CompletadoHoy) {
                                    return a1.getHora().compareTo(a2.getHora());
                                }
                                // Si solo una se ha completado hoy, se pone al final
                                if (a1CompletadoHoy) {
                                    return 1;
                                }
                                if (a2CompletadoHoy) {
                                    return -1;
                                }
                                // Si ninguna se ha completado hoy, se ordenan por hora
                                return a1.getHora().compareTo(a2.getHora());
                            });
                            // Notificamos el cambio al adaptador
                            adaptadorAlarmas.notifyDataSetChanged();
                            configurarAlarmas();
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


    // Método para obtener todos los eventos de un dni_usuario, enviando la solicitud al servidor
    public void mostrar_eventos(){
        final String URL = getString(R.string.IP) + "evento/lista?dni_usuario=" + dni;

        // Mandamos la solicitud
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Se añaden los eventos recibidos que tengan como fecha la actual a una lista
                        try {
                            eventos.clear();
                            LocalDate actual = LocalDate.now();
                            for (int i =0; i<response.length(); i++) {
                                JSONObject evento = response.getJSONObject(i);
                                String dni_usuario = evento.getString("dni_usuario");
                                String nombre = evento.getString("nombre");
                                String tipo = evento.getString("tipo");
                                String s_dia = evento.getString("dia");
                                LocalDate dia = LocalDate.parse(s_dia);
                                Boolean completado = evento.getBoolean("completado");
                                if (dia.equals(actual)) {
                                    eventos.add(new Evento(dni_usuario, nombre, tipo, dia, completado));
                                }
                            }
                            // Se ponen primero los no completados
                            eventos.sort((e1, e2) -> Boolean.compare(e1.getCompletado(), e2.getCompletado()));
                            // Notificamos el cambio al adaptador
                            adaptadorEventos.notifyDataSetChanged();
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


    // Método para editar un evento, enviando la solicitud al servidor
    public void actualizar_evento(Evento evento){

        // Creamos el JSON que vamos a enviar
        JSONObject new_evento= new JSONObject();
        try {
            new_evento.put("dni_usuario", evento.getDni_usuario());
            new_evento.put("nombre", evento.getNombre());
            new_evento.put("tipo", evento.getTipo());
            new_evento.put("dia", evento.getDia().toString());
            new_evento.put("completado", evento.getCompletado());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String nombreCodificado = "";
        String dniUsuarioCodificado = "";

        try {
            nombreCodificado = URLEncoder.encode(evento.getNombre(), "UTF-8");
            dniUsuarioCodificado = URLEncoder.encode(evento.getDni_usuario(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"evento?nombre=" + nombreCodificado + "&dni_usuario=" + dniUsuarioCodificado;

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_evento,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(getApplicationContext(), "evento actualizado", Toast.LENGTH_SHORT).show();
                                // En caso de que el evento  se actualice correctamente, se muestran de nuevo los eventos y las alarmas
                                mostrar_eventos();
                                mostrar_alarmas();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                Toast.makeText(getApplicationContext(),"error actualizando  el evento", Toast.LENGTH_SHORT).show();
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



    // Método para editar una alarma, enviando la solicitud al servidor
    public void actualizar_alarma(Alarma alarma, String hora){

        // Creamos el JSON que vamos a enviar
        JSONObject new_alarma= new JSONObject();
        try {
            new_alarma.put("dni_usuario", alarma.getDni_usuario());
            new_alarma.put("nombre", alarma.getNombre());
            new_alarma.put("tipo", alarma.getTipo());
            new_alarma.put("hora", Time.valueOf(hora));
            new_alarma.put("completado", alarma.getCompletado().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String nombreCodificado = "";
        String dniUsuarioCodificado = "";

        try {
            nombreCodificado = URLEncoder.encode(alarma.getNombre(), "UTF-8");
            dniUsuarioCodificado = URLEncoder.encode(alarma.getDni_usuario(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"alarma?nombre=" + nombreCodificado + "&dni_usuario=" + dniUsuarioCodificado;
        Log.d("MainUsuarios", "STRING De la nuevalarma" +new_alarma.toString());
        Log.d("MainUsuarios", "STRING DE la ULR " + URL);

        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new_alarma,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Actualizado")) {
                                //Toast.makeText(getApplicationContext(), "alarma actualizada", Toast.LENGTH_SHORT).show();
                                // En caso de que la alarma  se actualice correctamente, se muestran de nuevo los eventos y las alarmas
                                mostrar_alarmas();
                                mostrar_eventos();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                Toast.makeText(getApplicationContext(), "error actualizando  la alarma", Toast.LENGTH_SHORT).show();
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
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onDestroy() {
        Log.d("DESTROY","método onDestroy de la aplicación");
        super.onDestroy();
    }

    // Se registra el listener para poder escuchar eventos del acelerómetro.
    @Override
    protected void onResume() {
        Log.d("RESUME","método onResume de la aplicación");
        super.onResume();
        sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Método que se ejecuta cuando cambia el sensor, si el sensor que cambia es el acelerómetro,
    // se analizan los datos del evento para ver si se ha producido una caida o no
    public void onSensorChanged(SensorEvent event){
        Sensor sensor = event.sensor;
        if(sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            detectar_caida(event);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }



    //Método que evalua si se ha producido una caida o no según los datos del evento generado.
    private void detectar_caida(SensorEvent evento) {
        long now = System.currentTimeMillis();
        // Se calcula la fuerza de gravedad según los ejes X, Y y Z.
        // Si se supera el umbral se crea una incidencia
        if ((now - mShakeTime) > SHAKE_SLOP_TIME_MS) {
            mShakeTime = now;
            float gX = evento.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = evento.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = evento.values[2] / SensorManager.GRAVITY_EARTH;
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                confirmar_incidencia(dni);
            }
        }
    }



    // Método para confirmar la creación de una incidencia, si no se responde al dialogo, se crea
    private void confirmar_incidencia(String dni) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.incidencia);
        mediaPlayer.start();

        // Creamos el dialogo de confirmación
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView titulo = new TextView(this);
        titulo.setText("Incidencia detectada");
        titulo.setTextSize(30);
        titulo.setPadding(25, 25, 25, 25);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);
        builder.setCustomTitle(titulo);

        TextView mensaje = new TextView(this);
        mensaje.setText("Se ha detectado una incidencia. ¿Desea crearla?");
        mensaje.setTextSize(20);
        mensaje.setPadding(25, 25, 25, 25);;
        mensaje.setGravity(Gravity.CENTER);
        builder.setView(mensaje);


        builder.setCancelable(false)
                .setPositiveButton("CREAR", (dialog, which) -> {
                    Log.d("MainUsuarios", "Se ha pulsado CREAR");
                    crear_incidencia(dni);
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                })
                .setNegativeButton("NO CREAR", (dialog, which) -> {
                    Log.d("MainUsuarios", "Se ha pulsado NO CREAR");
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
        AlertDialog dialogo = builder.create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);
        dialogo.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(20);

        // Configuramos un temporizador para que ejecute automáticamente crear_incidencia después de 10 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialogo.isShowing()) {
                Log.d("MainUsuarios", "Creando incidencia automáticamente...");
                crear_incidencia(dni);
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                dialogo.dismiss();
            }
        }, 15000); // 15.000ms, 15 segundos
    }


    // Método para crear una incidencia, enviando la solicitud al servidor
    public void crear_incidencia(String dni){

        // Creamos el JSON que vamos a enviar
        JSONObject new_incidencia= new JSONObject();
        try {
            LocalTime hora_incidencia = LocalTime.now();
            int hora = hora_incidencia.getHour();
            int minuto = hora_incidencia.getMinute();
            String txhora = String.format("%02d:%02d:00", hora, minuto).trim();
            Log.d("MainUsuarios", "STRING DE la hora " + txhora);
            Log.d("MainUsuarios", "STRING DE la hora (length " + txhora.length() + "): '" + txhora + "'");
            LocalDate fecha_incidencia = LocalDate.now();
            int dia = fecha_incidencia.getDayOfMonth();
            int mes = fecha_incidencia.getMonthValue();
            int ano = fecha_incidencia.getYear();
            String txfecha = String.format("%04d-%02d-%02d", ano, mes,dia);
            Log.d("MainUsuarios", "STRING DE la fecha: " + txfecha);
            new_incidencia.put("dni_usuario", dni);
            new_incidencia.put("resuelta", false);
            new_incidencia.put("fecha", txfecha);
            new_incidencia.put("hora", txhora);
            Log.d("MainUsuarios", "STRING DE la incidencia: " + new_incidencia.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String URL = getString(R.string.IP)  +"incidencia";


        // Mandamos la solicitud
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, new_incidencia,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("message").equals("Insertado")) {
                                Toast.makeText(getApplicationContext(), "Se ha registrado una incidencia, en breve le llamará un operador", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                // En caso de error se imprime un Toast por pantalla para informar al usuario
                Toast.makeText(getApplicationContext(), "error insertando la incidencia", Toast.LENGTH_SHORT).show();
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



    // Método que muestra un diálogo cuando se activa una alarma.
    private void showAlarmDialog(String nombre, String hora) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.alarma);
        mediaPlayer.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView titulo = new TextView(this);
        titulo.setText("Alarma: " + hora);
        titulo.setTextSize(30);
        titulo.setPadding(25, 25, 25, 25);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);
        builder.setCustomTitle(titulo);

        TextView mensaje = new TextView(this);
        mensaje.setText("Es hora de tu alarma: " + nombre);
        mensaje.setTextSize(20);
        mensaje.setPadding(25, 25, 25, 25);
        mensaje.setGravity(Gravity.CENTER);
        builder.setView(mensaje);

        builder.setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
        AlertDialog dialogo = builder.create();
        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(20);

    }



    //Método que configura alarmas con el SO mediante el AlarmManager.
    private void configurarAlarmas() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.d("MainUsuarios", "Configurando las alarms");

        // Para cada alarma se crea una alarma a la hora correspondiente
        for (Alarma alarma : alarmas) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarma.getHora().getHour());
            calendar.set(Calendar.MINUTE, alarma.getHora().getMinute());
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Intent intent = new Intent(this, AlarmaService.class);
            intent.putExtra("showDialog", true);
            intent.putExtra("nombre", alarma.getNombre());
            intent.putExtra("hora", alarma.getHora().toString());
            String alarmKey = alarma.getNombre() + "_" + alarma.getHora().toString();
            Log.d("MainUsuarios", "Alarma: " + alarma.getNombre() + " hora: " + alarma.getHora().toString());

            // Verificar si la alarma ya existe
            PendingIntent existingIntent = PendingIntent.getService(
                    this,
                    alarmKey.hashCode(),
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (existingIntent == null) {
                PendingIntent pendingIntent = PendingIntent.getService(
                        this,
                        alarmKey.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("MainUsuarios", "Alarma configurada para: " + calendar.getTime());
            } else {
                Log.d("MainUsuarios", "La alarma ya existe, no se creará de nuevo: " + alarmKey);
            }

        }
    }






}