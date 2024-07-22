package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);

        if (FirebaseUtils.isLoggedIn()) {
            FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtils.currentUserModel = task.getResult().toObject(UserModel.class);
                    Log.d("SplashScreen", "isAdmin -> " + FirebaseUtils.currentUserModel.getIsAdmin());
                    Log.d("SplashScreen", "userName -> " + FirebaseUtils.currentUserModel.getFull_name());
                    Log.d("SplashScreen", "isBanned -> " + FirebaseUtils.currentUserModel.getIsBanned());

                    // Avvia MainActivity dopo il caricamento di UserModel
                    new Handler().postDelayed(() -> {
                        Intent intent;
                        if(FirebaseUtils.currentUserModel.getIsBanned() == 1){
                            intent = new Intent(SplashScreen.this, Login.class);
                            AndroidUtils.showToast(getApplicationContext(), "You Was Banned by and Administrator");
                        }
                        else intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, SPLASH_TIME_OUT);
                } else {
                    // Gestisci il caso di errore se necessario
                    Log.e("TAG", "Error loading user model", task.getException());
                }
            });
        } else {
            // Avvia LoginActivity se non loggato
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
                finish();
            }, SPLASH_TIME_OUT);
        }
    }
}