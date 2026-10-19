package com.example.video_practica;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btnPlay, btnPause, btnStop, btnRetroceder, btnAdelantar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView     = findViewById(R.id.videoView);
        btnPlay       = findViewById(R.id.btnPlay);
        btnPause      = findViewById(R.id.btnPause);
        btnStop       = findViewById(R.id.btnStop);
        btnRetroceder = findViewById(R.id.btnRetroceder);
        btnAdelantar  = findViewById(R.id.btnAdelantar);

        cargarVideo();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    videoView.start();
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.stopPlayback();
                cargarVideo();
            }
        });

        btnRetroceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = videoView.getCurrentPosition() - 10000;
                videoView.seekTo(Math.max(pos, 0));
            }
        });

        btnAdelantar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = videoView.getCurrentPosition() + 10000;
                videoView.seekTo(Math.min(pos, videoView.getDuration()));
            }
        });
    }

    private void cargarVideo() {
        Uri uri = Uri.parse("android.resource://" + getPackageName()
                + "/" + R.raw.videoplayback);
        videoView.setVideoURI(uri);
        videoView.seekTo(1);   // muestra el primer cuadro
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}