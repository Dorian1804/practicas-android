package com.example.audios_android;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Archivos de audio guardados en res/raw
    private final int[] canciones = {R.raw.cancion1, R.raw.cancion2, R.raw.cancion3};
    private final String[] titulos = {"Cancion 1", "Cancion 2", "Cancion 3"};

    private MediaPlayer mediaPlayer;          // Objeto de audio
    private int indice = 0;

    private TextView tvTitulo, tvTiempo;
    private SeekBar seekBar;
    private Button btnPlay, btnPause, btnStop, btnAnterior, btnSiguiente;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable actualizarProgreso = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                tvTiempo.setText(formato(pos) + " / " + formato(mediaPlayer.getDuration()));
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitulo = findViewById(R.id.tvTitulo);
        tvTiempo = findViewById(R.id.tvTiempo);
        seekBar = findViewById(R.id.seekBar);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        cargarCancion(indice);

        btnPlay.setOnClickListener(v -> reproducir());
        btnPause.setOnClickListener(v -> pausar());
        btnStop.setOnClickListener(v -> detener());
        btnAnterior.setOnClickListener(v -> cambiarCancion(-1));
        btnSiguiente.setOnClickListener(v -> cambiarCancion(1));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progreso, boolean delUsuario) {
                if (delUsuario && mediaPlayer != null) {
                    mediaPlayer.seekTo(progreso);
                    tvTiempo.setText(formato(progreso) + " / " + formato(mediaPlayer.getDuration()));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
    }

    // Crea el MediaPlayer con el archivo de audio indicado
    private void cargarCancion(int i) {
        liberarPlayer();
        mediaPlayer = MediaPlayer.create(this, canciones[i]);
        tvTitulo.setText(titulos[i]);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
        tvTiempo.setText("00:00 / " + formato(mediaPlayer.getDuration()));

        // Al terminar la cancion pasa a la siguiente automaticamente
        mediaPlayer.setOnCompletionListener(mp -> {
            cambiarCancion(1);
        });
    }

    private void reproducir() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            handler.post(actualizarProgreso);
        }
    }

    private void pausar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void detener() {
        // Despues de stop() el MediaPlayer no se puede reiniciar, por eso se vuelve a crear
        cargarCancion(indice);
    }

    // direccion: 1 = siguiente, -1 = anterior
    private void cambiarCancion(int direccion) {
        indice = (indice + direccion + canciones.length) % canciones.length;
        cargarCancion(indice);
        reproducir();
    }

    private String formato(int ms) {
        int seg = ms / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", seg / 60, seg % 60);
    }

    private void liberarPlayer() {
        handler.removeCallbacks(actualizarProgreso);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liberarPlayer();
    }
}