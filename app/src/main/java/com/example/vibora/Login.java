package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mRegisterBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initViews();

        fAuth = FirebaseAuth.getInstance();

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    AndroidUtils.hideKeyboardFrom(getApplicationContext(), mPassword);
                    login();
                    return true;
                }
                return false;
            }
        });

        mLoginBtn.setOnClickListener(v -> {
            login();
        });

        mRegisterBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
        });
    }

    private void initViews() {
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.register_text_button);
        mLoginBtn = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progressBar2);
    }

    //==============================================================================================

    private void login(){
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            mEmail.setError("Email is Required");
            return;
        }
        if(TextUtils.isEmpty(password)){
            mPassword.setError("Password is Required");
            return;
        }
        if(password.length() < 6){
            mPassword.setError("Password must be at least 6 Characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Login the user

        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task1 -> {
                                FirebaseUtils.currentUserModel = task1.getResult().toObject(UserModel.class);
                                Intent intent;
                                Log.d("Login", "isAdmin -> " + FirebaseUtils.currentUserModel.getIsAdmin());
                                Log.d("Login", "userName -> " + FirebaseUtils.currentUserModel.getFull_name());
                                Log.d("Login", "isBanned -> " + FirebaseUtils.currentUserModel.getIsBanned());
                                if(FirebaseUtils.currentUserModel.getIsBanned() == 1){
                                    intent = new Intent(getApplicationContext(), Login.class);
                                    AndroidUtils.showToast(getApplicationContext(), "You Was Banned by and Administrator");
                                }
                                else intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            Toast.makeText(Login.this, "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}