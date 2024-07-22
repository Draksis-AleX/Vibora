package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.vibora.adapter.FieldRecyclerAdapter;
import com.example.vibora.model.FieldModel;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {
    Button mybookings_btn;
    RecyclerView recyclerView;
    FieldRecyclerAdapter adapter;
    FirestoreRecyclerOptions<FieldModel> options;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.getActivity().getWindow().setStatusBarColor(this.getResources().getColor(R.color.main_green));

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        setupFieldRecyclerView();

        mybookings_btn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MyBookings.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupFieldRecyclerView();
    }

    private void initViews(View view) {
        mybookings_btn = view.findViewById(R.id.mybookings_btn);
        recyclerView = view.findViewById(R.id.field_recycler);
    }

    //==============================================================================================

    private void setupFieldRecyclerView(){
        Query query = FirebaseUtils.allFieldsCollectionReference();

        // Costruisci nuove opzioni per l'adapter
        FirestoreRecyclerOptions<FieldModel> newOptions = new FirestoreRecyclerOptions.Builder<FieldModel>()
                .setQuery(query, FieldModel.class).build();

        // Se l'adapter non è stato inizializzato o la query è cambiata, aggiorna l'adapter
        if (adapter == null || !newOptions.equals(options)) {
            options = newOptions; // Aggiorna le opzioni memorizzate
            adapter = new FieldRecyclerAdapter(options, requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }
}