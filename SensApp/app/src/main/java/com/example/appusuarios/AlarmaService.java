// Este archivo define un servicio que se encarga de manejar alarmas y mostrar
// un diálogo cuando es la hora de alguna de las alarmas.

package com.example.appusuarios;

// Imports
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class AlarmaService extends Service {
    public boolean mostrado = false;
    private MediaPlayer mediaPlayer;
     public static final String CHANNEL_ID = "AlarmaServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Si la versión es compatible,se crea un canal de notificaciones para el servicio de alarmas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarma Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }

    // Método que define el comportamiento cuando se recibe un comando: se muestra el diálogo de la alarma
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && !mostrado) {
            String nombre = intent.getStringExtra("nombre");
            String hora = intent.getStringExtra("hora");
            Log.d("AlarmService", "Recibido en AlarmaService - Nombre: " + nombre + ", Hora: " + hora);

            // Muestra el diálogo
            showAlarmDialog(nombre, hora);
            mostrado = true;
        }else{
            Log.d("AlarmService", "Dialogo mostrado");
          }
        return START_NOT_STICKY;
    }

    // Método que inicia la actividad MainUsuarios con el diálogo de la alarma
    private void showAlarmDialog(String nombre, String hora) {
        Log.d("AlarmaService", "showAlarmDialog() called");
        mediaPlayer = MediaPlayer.create(this, R.raw.alarma);
        mediaPlayer.start();

        Intent dialogIntent = new Intent(this, MainUsuarios.class);
        dialogIntent.putExtra("showDialog", true);
        dialogIntent.putExtra("nombre", nombre);
        dialogIntent.putExtra("hora", hora);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mostrado = false;
        Log.d("AlarmService", "Servicio destruido");
    }

}
