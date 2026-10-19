package com.example.practica_31_23300167_6d1_pm1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
Button btnTocame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
    btnTocame = findViewById(R.id.btnTocame);
    btnTocame.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        startActivity( new Intent(MainActivity.this, MensajeActivity.class));
        }
    });
    }
}