package com.example.vibora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.FieldRecyclerAdapter;
import com.example.vibora.model.FieldModel;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SetLessons extends AppCompatActivity {
    Button back_btn;
    RecyclerView recyclerView;
    FieldRecyclerAdapter adapter;
    FirestoreRecyclerOptions<FieldModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_lessons);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.main_green));

        initWidgets();
        setupFieldRecyclerView();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupFieldRecyclerView(){
        // Costruisci la nuova query con il termine di ricerca
        Query query = FirebaseUtils.allFieldsCollectionReference();

        // Costruisci nuove opzioni per l'adapter
        FirestoreRecyclerOptions<FieldModel> newOptions = new FirestoreRecyclerOptions.Builder<FieldModel>()
                .setQuery(query, FieldModel.class).build();

        // Se l'adapter non è stato inizializzato o la query è cambiata, aggiorna l'adapter
        if (adapter == null || !newOptions.equals(options)) {
            options = newOptions; // Aggiorna le opzioni memorizzate
            adapter = new FieldRecyclerAdapter(options, this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }

    private void initWidgets() {
        back_btn = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.field_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}