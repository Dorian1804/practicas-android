package com.example.imagenes_android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView imgLogo;
    private ImageButton btnImagen;
    private Button btnTexto;
    private boolean cambiada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgLogo   = findViewById(R.id.imgLogo);
        btnImagen = findViewById(R.id.btnImagen);
        btnTexto  = findViewById(R.id.btnTexto);

        // El ImageButton cambia la imagen del ImageView
        btnImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cambiada) {
                    imgLogo.setImageResource(R.drawable.logo);
                } else {
                    imgLogo.setImageResource(R.drawable.logo2);
                }
                cambiada = !cambiada;
                Toast.makeText(MainActivity.this,
                        "Imagen cambiada", Toast.LENGTH_SHORT).show();
            }
        });

        // El botón normal muestra un mensaje
        btnTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Botón con icono presionado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Guardar el estado al rotar la pantalla
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("cambiada", cambiada);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cambiada = savedInstanceState.getBoolean("cambiada");
        imgLogo.setImageResource(cambiada ? R.drawable.logo2 : R.drawable.logo);
    }
}