package com.example.vibora;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.ReportsAdapter;
import com.example.vibora.model.ReportModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewAllReports extends AppCompatActivity {
    RecyclerView reports_recycler;
    ArrayList<ReportModel> reports_list;
    Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_green));
        setContentView(R.layout.activity_view_all_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setReportsRecycler();

        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        reports_recycler = findViewById(R.id.reports_recycler);
        back_btn = findViewById(R.id.back_btn);
    }

    //==============================================================================================

    private void setReportsRecycler() {
        reports_list = new ArrayList<>();
        FirebaseFirestore.getInstance().collectionGroup("reports").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(task.isSuccessful()){
                            for(DocumentSnapshot doc : querySnapshot.getDocuments()){
                                ReportModel reportModel = doc.toObject(ReportModel.class);
                                reports_list.add(reportModel);
                            }

                            ReportsAdapter adapter = new ReportsAdapter(getApplicationContext(), reports_list);
                            reports_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            reports_recycler.setAdapter(adapter);
                        }
                    }
                });
    }
}