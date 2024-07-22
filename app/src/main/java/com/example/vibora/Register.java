package com.example.vibora;

import static android.content.ContentValues.TAG;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {

    EditText mFullName, mUsername, mEmail, mPassword, mConfirmPassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        initWidgets();

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    AndroidUtils.hideKeyboardFrom(getApplicationContext(), mPassword);
                    register();
                    return true;
                }
                return false;
            }
        });


        mRegisterBtn.setOnClickListener(v -> {
            register();
        });

        mLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initWidgets() {
        mFullName = findViewById(R.id.full_name);
        mUsername = findViewById(R.id.username);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mConfirmPassword = findViewById(R.id.confirm_password);
        mRegisterBtn = findViewById(R.id.register_button);
        mLoginBtn = findViewById(R.id.login_text_btn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void register(){
        String full_name = mFullName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirm_password = mConfirmPassword.getText().toString().trim();

        if(TextUtils.isEmpty(full_name)){
            mFullName.setError("Full Name is Required");
            return;
        }
        if(TextUtils.isEmpty(email)){
            mEmail.setError("Email is Required");
            return;
        }
        if(TextUtils.isEmpty(username)){
            mUsername.setError("Username is Required");
            return;
        }
        if(TextUtils.isEmpty(password)){
            mPassword.setError("Password is Required");
            return;
        }
        if(TextUtils.isEmpty(confirm_password)){
            mConfirmPassword.setError("Password is Required");
            return;
        }
        if(password.length() < 6){
            mPassword.setError("Password must be at least 6 Characters");
            return;
        }
        if(!password.equals(confirm_password)){
            mConfirmPassword.setError("Passwords don't match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Register the user

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "User Created", Toast.LENGTH_SHORT).show();

                            userID = FirebaseUtils.currentUserId();
                            DocumentReference docReference = FirebaseUtils.currentUserDetails();
                            UserModel user = new UserModel(full_name, username, email, 0, FirebaseUtils.currentUserId());

                            docReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "OnSuccess: user profile is created for " + userID + " | " + user.getFull_name() + " | " + user.getUsername() + " | " + user.getEmail());
                                    FirebaseUtils.currentUserDetails().get().addOnCompleteListener(task1 -> {
                                        FirebaseUtils.currentUserModel = task1.getResult().toObject(UserModel.class);
                                        Log.d("TAG", "isAdmin -> " + FirebaseUtils.currentUserModel.getIsAdmin());
                                        Log.d("TAG", "userName -> " + FirebaseUtils.currentUserModel.getFull_name());
                                        Toast.makeText(Register.this, "Logged In", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    });
                                }
                            });
                        } else {
                            AndroidUtils.showToast(Register.this, "Registration Failed! " + task.getException().getMessage());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
}