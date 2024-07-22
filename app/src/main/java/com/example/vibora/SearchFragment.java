package com.example.vibora;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.vibora.adapter.SearchUserRecyclerAdapter;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchFragment extends Fragment {
    EditText searchInput;
    ImageButton searchButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;
    FirestoreRecyclerOptions<UserModel> options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.getActivity().getWindow().setStatusBarColor(this.getResources().getColor(R.color.main_green));

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initViews(view);

        searchInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String searchTerm = searchInput.getText().toString();
                    if(searchTerm.isEmpty()){
                        searchInput.setError("Invalid Username");
                        return false;
                    }
                    AndroidUtils.hideKeyboardFrom(getContext(), searchInput);
                    setupSearchRecyclerView(searchTerm);
                    return true;
                }
                return false;
            }
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty()){
                searchInput.setError("Invalid Username");
                return;
            }
            AndroidUtils.hideKeyboardFrom(getContext(), searchInput);
            setupSearchRecyclerView(searchTerm);
        });

        return view;
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.search_username_input);
        searchButton = view.findViewById(R.id.search_user_btn);
        recyclerView = view.findViewById(R.id.search_user_recycler_view);

        searchInput.requestFocus();
    }

    //==============================================================================================

    void setupSearchRecyclerView(String searchTerm){

        // Costruisci la nuova query con il termine di ricerca
        Query query = FirebaseUtils.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username", searchTerm);

        // Costruisci nuove opzioni per l'adapter
        FirestoreRecyclerOptions<UserModel> newOptions = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        // Se l'adapter non è stato inizializzato o la query è cambiata, aggiorna l'adapter
        if (adapter == null || !newOptions.equals(options)) {
            options = newOptions; // Aggiorna le opzioni memorizzate
            adapter = new SearchUserRecyclerAdapter(options, requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null) adapter.stopListening();
        recyclerView.setAdapter(null); // Rimuove l'adapter dalla RecyclerView
        adapter = null; // Pulisce il riferimento all'adapter
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null) adapter.startListening();
    }
}