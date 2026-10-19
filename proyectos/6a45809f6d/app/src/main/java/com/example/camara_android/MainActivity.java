package com.example.camara_android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView imgFoto;
    private TextView txtRuta;
    private Button btnFoto;
    private File archivoFoto;

    // Abre la cámara y guarda la foto en el Uri indicado
    private final ActivityResultLauncher<Uri> camaraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito) {
                    mostrarFoto();
                } else {
                    Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show();
                }
            });

    // Pide el permiso de cámara en tiempo de ejecución
    private final ActivityResultLauncher<String> permisoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        txtRuta = findViewById(R.id.txtRuta);
        btnFoto = findViewById(R.id.btnFoto);

        btnFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                permisoLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Recuperar la foto si la pantalla se rotó
        if (savedInstanceState != null) {
            String ruta = savedInstanceState.getString("ruta");
            if (ruta != null) {
                archivoFoto = new File(ruta);
                if (archivoFoto.exists()) {
                    mostrarFoto();
                }
            }
        }
    }

    private void abrirCamara() {
        try {
            archivoFoto = crearArchivo();
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", archivoFoto);
            camaraLauncher.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivo() throws IOException {
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File carpeta = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("FOTO_" + fecha + "_", ".jpg", carpeta);
    }

    private void mostrarFoto() {
        // Se reduce la imagen para no gastar demasiada memoria
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = 4;
        Bitmap bmp = BitmapFactory.decodeFile(archivoFoto.getAbsolutePath(), opciones);
        imgFoto.setImageBitmap(bmp);

        txtRuta.setText("Archivo creado:\n" + archivoFoto.getAbsolutePath()
                + "\nTamaño: " + (archivoFoto.length() / 1024) + " KB");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (archivoFoto != null) {
            outState.putString("ruta", archivoFoto.getAbsolutePath());
        }
    }
}