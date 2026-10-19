package com.example.appnotificaciones;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // Canales para dos tipos de notificaciones distintas
    public static final String CANAL_INFO = "canal_info";
    public static final String CANAL_ALERTA = "canal_alerta";

    private static final int PERMISO_NOTIFICACIONES = 100;

    private Button btnPedirPermiso, btnNotifInfo, btnNotifAlerta;
    private Button btnIniciarServicio, btnDetenerServicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        crearCanalesDeNotificacion();

        btnPedirPermiso = findViewById(R.id.btnPedirPermiso);
        btnNotifInfo = findViewById(R.id.btnNotifInfo);
        btnNotifAlerta = findViewById(R.id.btnNotifAlerta);
        btnIniciarServicio = findViewById(R.id.btnIniciarServicio);
        btnDetenerServicio = findViewById(R.id.btnDetenerServicio);

        btnPedirPermiso.setOnClickListener(v -> solicitarPermisoNotificaciones());

        btnNotifInfo.setOnClickListener(v -> mostrarNotificacionInformativa());

        btnNotifAlerta.setOnClickListener(v -> mostrarNotificacionAlerta());

        btnIniciarServicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyForegroundService.class);
            ContextCompat.startForegroundService(MainActivity.this, intent);
            Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_SHORT).show();
        });

        btnDetenerServicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyForegroundService.class);
            stopService(intent);
            Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
        });
    }

    // Crea los canales de notificación (obligatorio en Android 8.0 / API 26+)
    private void crearCanalesDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            NotificationChannel canalInfo = new NotificationChannel(
                    CANAL_INFO,
                    "Notificaciones Informativas",
                    NotificationManager.IMPORTANCE_DEFAULT);
            canalInfo.setDescription("Canal para mensajes informativos de la app");

            NotificationChannel canalAlerta = new NotificationChannel(
                    CANAL_ALERTA,
                    "Notificaciones de Alerta",
                    NotificationManager.IMPORTANCE_HIGH);
            canalAlerta.setDescription("Canal para alertas importantes de la app");

            manager.createNotificationChannel(canalInfo);
            manager.createNotificationChannel(canalAlerta);
        }
    }

    // Solicita en tiempo de ejecución el permiso POST_NOTIFICATIONS (Android 13+)
    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISO_NOTIFICACIONES);
            } else {
                Toast.makeText(this, "El permiso ya estaba concedido", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se requiere permiso en esta versión de Android", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_NOTIFICACIONES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Notificación de tipo 1: informativa
    private void mostrarNotificacionInformativa() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_INFO)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Información")
                .setContentText("Esta es una notificación informativa de la app.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        enviarNotificacion(1, builder);
    }

    // Notificación de tipo 2: alerta (distinta naturaleza: prioridad alta, icono distinto)
    private void mostrarNotificacionAlerta() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ALERTA)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("¡Alerta!")
                .setContentText("Esta es una notificación de alerta importante.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        enviarNotificacion(2, builder);
    }

    private void enviarNotificacion(int id, NotificationCompat.Builder builder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, "Primero concede el permiso de notificaciones", Toast.LENGTH_SHORT).show();
            return;
        }
        NotificationManagerCompat.from(this).notify(id, builder.build());
    }
}