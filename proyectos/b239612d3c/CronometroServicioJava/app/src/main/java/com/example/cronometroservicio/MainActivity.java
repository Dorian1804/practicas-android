package com.example.cronometroservicio;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTiempo;
    private Button btnIniciar;
    private Button btnPausar;
    private Button btnReiniciar;

    private TimerService timerService;
    private boolean isBound = false;

    // Solicita el permiso de notificaciones en Android 13+ (necesario para el Foreground Service)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // No se requiere accion extra con el resultado del permiso
            });

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isBound = true;

            // Al conectarse, observamos el LiveData del servicio: si la app se minimizo
            // y el conteo siguio en segundo plano, la UI se actualiza inmediatamente
            // con el valor real acumulado.
            timerService.getElapsedSeconds().observe(MainActivity.this, segundos ->
                    tvTiempo.setText(formatearTiempo(segundos)));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTiempo = findViewById(R.id.tvTiempo);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnPausar = findViewById(R.id.btnPausar);
        btnReiniciar = findViewById(R.id.btnReiniciar);

        pedirPermisoNotificaciones();

        btnIniciar.setOnClickListener(v -> {
            // startForegroundService asegura que el servicio siga vivo aunque
            // la Activity se destruya o la app pase a segundo plano.
            Intent intent = new Intent(this, TimerService.class);
            ContextCompat.startForegroundService(this, intent);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        });

        btnPausar.setOnClickListener(v -> {
            if (isBound) {
                timerService.pausarConteo();
            }
        });

        btnReiniciar.setOnClickListener(v -> {
            if (isBound) {
                timerService.reiniciarConteo();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Si el servicio ya estaba corriendo en segundo plano (la app se reabrio),
        // nos enlazamos de nuevo para recuperar el estado actual del conteo.
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Solo nos desenlazamos de la UI; el servicio sigue ejecutandose
        // en segundo plano porque fue iniciado con startForegroundService.
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private String formatearTiempo(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }
}
