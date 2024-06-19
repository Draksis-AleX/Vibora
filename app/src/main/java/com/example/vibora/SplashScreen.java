package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Imposta la durata della schermata di benvenuto (in millisecondi)
        int SPLASH_TIME_OUT = 3000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Avvia l'attività principale
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                // Chiudi questa attività
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}