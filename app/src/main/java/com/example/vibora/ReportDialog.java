package com.example.vibora;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibora.model.ReportModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDate;

public class ReportDialog extends AppCompatActivity {
    String USER_ID, USERNAME;
    TextView user_txt;
    Spinner spinner;
    Button confirm_btn, cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_dialog);

        USER_ID = getIntent().getStringExtra("USER_ID");
        USERNAME = getIntent().getStringExtra("USERNAME");

        initViews();
        setupSpinner();

        confirm_btn.setOnClickListener(v -> {
            reportPlayer();
            finish();
        });

        cancel_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        user_txt = findViewById(R.id.user_txt);
        confirm_btn = findViewById(R.id.confirm_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        spinner = findViewById(R.id.reason_spinner);

        user_txt.setText(USERNAME);
    }

    //==============================================================================================

    private void reportPlayer() {
        ReportModel reportModel = new ReportModel(USER_ID, USERNAME, CalendarUtils.convertFromLocalDateToTimestamp(LocalDate.now()), spinner.getSelectedItem().toString(), FirebaseUtils.currentUserId(), FirebaseUtils.currentUserModel.getUsername());
        FirebaseUtils.userReportsCollectionReference(USER_ID).add(reportModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                AndroidUtils.showToast(getApplicationContext(), "User Reported");
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.report_reasons, R.layout.spinner_item_text);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }
}