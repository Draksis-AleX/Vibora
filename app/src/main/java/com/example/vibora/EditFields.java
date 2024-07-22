package com.example.vibora;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.AdminFieldAdapter;
import com.example.vibora.model.FieldModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EditFields extends AppCompatActivity {
    RecyclerView fields_recycler;
    EditText new_field_name;
    Button add_field_btn, back_btn;
    AdminFieldAdapter adapter;
    ArrayList<FieldModel> field_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_fields);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setFieldRecycler();

        add_field_btn.setOnClickListener(v -> {
            addNewField();
        });

        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        fields_recycler = findViewById(R.id.fields_recycler);
        new_field_name = findViewById(R.id.field_name);
        add_field_btn = findViewById(R.id.add_field_btn);
        back_btn = findViewById(R.id.back_btn);
    }

    //==============================================================================================

    private void setFieldRecycler() {
        field_list = new ArrayList<>();

        FirebaseUtils.allFieldsCollectionReference().get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            FieldModel fieldModel = doc.toObject(FieldModel.class);
                            field_list.add(fieldModel);
                        }

                        adapter = new AdminFieldAdapter(getApplicationContext(), field_list);
                        fields_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        fields_recycler.setAdapter(adapter);
                    }
                });
    }

    private void addNewField(){
        if(TextUtils.isEmpty(new_field_name.getText().toString())){
            new_field_name.setError("Field Name Required");
            return;
        }

        String new_field_name_string = new_field_name.getText().toString();
        FieldModel fieldModel = new FieldModel(new_field_name_string, new_field_name_string, 0, 0);

        FirebaseUtils.allFieldsCollectionReference().document(new_field_name_string).set(fieldModel)
                .addOnSuccessListener(aVoid -> {
                    AndroidUtils.showToast(getApplicationContext(), "New Field Added");
                    field_list.add(0, fieldModel);
                    adapter.updateFieldList(field_list);
                    new_field_name.setText("");
                });
    }
}

