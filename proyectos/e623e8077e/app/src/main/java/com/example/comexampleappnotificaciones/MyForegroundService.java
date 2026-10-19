package com.example.appnotificaciones;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MyForegroundService extends Service {

    private static final int ID_NOTIFICACION_SERVICIO = 3;
    private static final String TAG = "MyForegroundService";

    private Handler handler;
    private Runnable tareaPeriodica;
    private int contador = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Notificación de tipo 3: notificación fija del servicio en ejecución
        Notification notification = new NotificationCompat.Builder(this, MainActivity.CANAL_INFO)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Servicio activo")
                .setContentText("El servicio en segundo plano está corriendo...")
                .setOngoing(true)
                .build();

        // Convierte el servicio en un Foreground Service (obligatorio desde Android 8+)
        startForeground(ID_NOTIFICACION_SERVICIO, notification);

        // Tarea que se repite cada 5 segundos para demostrar que el servicio sigue vivo
        tareaPeriodica = new Runnable() {
            @Override
            public void run() {
                contador++;
                Log.d(TAG, "Servicio en ejecución, ciclo #" + contador);
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(tareaPeriodica);

        // START_STICKY: si el sistema mata el servicio, intentará recrearlo
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No se usa comunicación por binding en esta práctica
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && tareaPeriodica != null) {
            handler.removeCallbacks(tareaPeriodica);
        }
        Log.d(TAG, "Servicio detenido");
    }
}