package com.example.cronometroservicio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

/**
 * Servicio en primer plano (Foreground Service) que lleva el conteo de segundos
 * del cronometro. Al ser un Foreground Service con notificacion persistente,
 * Android no lo destruye cuando la app pasa a segundo plano, por lo que el
 * conteo continua aunque el usuario minimice la app o cambie a otra.
 */
public class TimerService extends Service {

    public static final String CHANNEL_ID = "cronometro_channel";
    public static final int NOTIFICATION_ID = 1;

    // Binder que se entrega a la Activity cuando se enlaza (bind) al servicio
    private final IBinder binder = new TimerBinder();

    // Handler para ejecutar el conteo cada 1000 ms en el hilo principal del servicio
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Segundos transcurridos, expuestos como LiveData para que la Activity
    // pueda observar los cambios y actualizar la UI automaticamente.
    private final MutableLiveData<Integer> elapsedSeconds = new MutableLiveData<>(0);

    private boolean isRunning = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                Integer actual = elapsedSeconds.getValue();
                int nuevoValor = (actual == null ? 0 : actual) + 1;
                elapsedSeconds.setValue(nuevoValor);
                actualizarNotificacion(nuevoValor);
                handler.postDelayed(this, 1000);
            }
        }
    };

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public MutableLiveData<Integer> getElapsedSeconds() {
        return elapsedSeconds;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalNotificacion();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Se inicia como Foreground Service: muestra notificacion obligatoria
        // y evita que el sistema mate el proceso al minimizar la app.
        Integer actual = elapsedSeconds.getValue();
        startForeground(NOTIFICATION_ID, construirNotificacion(actual == null ? 0 : actual));
        iniciarConteo();
        // START_STICKY: si el sistema mata el servicio por falta de recursos,
        // intentara recrearlo automaticamente.
        return START_STICKY;
    }

    public void iniciarConteo() {
        if (!isRunning) {
            isRunning = true;
            handler.post(timerRunnable);
        }
    }

    public void pausarConteo() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void reiniciarConteo() {
        pausarConteo();
        elapsedSeconds.setValue(0);
        actualizarNotificacion(0);
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Cronometro en segundo plano",
                    NotificationManager.IMPORTANCE_LOW
            );
            canal.setDescription("Muestra el progreso del cronometro mientras se ejecuta en segundo plano");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

    private Notification construirNotificacion(int segundos) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cronometro en ejecucion")
                .setContentText("Tiempo transcurrido: " + formatearTiempo(segundos))
                .setSmallIcon(android.R.drawable.ic_menu_recent_history)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void actualizarNotificacion(int segundos) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, construirNotificacion(segundos));
    }

    private String formatearTiempo(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    @Override
    public void onDestroy() {
        pausarConteo();
        super.onDestroy();
    }
}
