package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.SearchUserRecyclerAdapter;
import com.example.vibora.model.BookingModel;
import com.example.vibora.model.PlayerResult;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Booking extends AppCompatActivity{
    static String FIELD_NAME, DATE, TIMESLOT;
    int RESERVED_SPOTS;
    TextView field_text, date_text, timeslot_text, reservedspots_text;
    RecyclerView userlist_recycler;
    SearchUserRecyclerAdapter adapter;
    FirestoreRecyclerOptions<UserModel> options;
    Button book_btn, back_btn, reserve_btn;
    boolean recyclerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.main_green));
        setContentView(R.layout.activity_booking);

        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        DATE = getIntent().getStringExtra("DATE");
        TIMESLOT = getIntent().getStringExtra("TIMESLOT");
        RESERVED_SPOTS = getIntent().getIntExtra("RESERVED_SPOTS", 0);

        initViews();
        setupUserRecyclerView();

        back_btn.setOnClickListener(v -> {
            finish();
        });

        book_btn.setOnClickListener(v -> {
            book();
        });

        reserve_btn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ReserveDialog.class);
            intent.putExtra("FIELD_NAME", FIELD_NAME);
            intent.putExtra("TIMESLOT", TIMESLOT);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(recyclerInitialized) setupUserRecyclerView();
        recyclerInitialized = true;
    }

    private void initViews() {
        field_text = findViewById(R.id.field_txt);
        field_text.setText("Booking on " + FIELD_NAME);
        date_text = findViewById(R.id.date_txt);
        date_text.setText(DATE);
        timeslot_text = findViewById(R.id.timeslot_txt);
        timeslot_text.setText(TIMESLOT);
        reservedspots_text = findViewById(R.id.reservedspots_txt);
        reservedspots_text.setText(RESERVED_SPOTS + " / 4");

        userlist_recycler = findViewById(R.id.userlist_recycler);
        book_btn = findViewById(R.id.book_btn);
        back_btn = findViewById(R.id.back_btn);
        reserve_btn = findViewById(R.id.reserve_btn);
    }

    //==============================================================================================

    private void setupUserRecyclerView() {
        FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, DATE).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        BookingModel bookingModel = null;
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            bookingModel = doc.toObject(BookingModel.class);
                            if(CalendarUtils.mapIndexToTimeSlot(bookingModel.getTimeSlot()).equals(TIMESLOT)){
                                break;
                            }
                            else bookingModel = null;
                        }

                        if (bookingModel == null) return;
                        
                        Query query = FirebaseUtils.allUserCollectionReference().whereIn(FieldPath.documentId(), bookingModel.getUserIdList());

                        if(bookingModel.getUserIdList().contains(FirebaseUtils.currentUserId())){
                            ViewGroup layout = (ViewGroup) reserve_btn.getParent();
                            if(layout!=null && book_btn != null) layout.removeView(book_btn);
                            if(bookingModel.isReserved()) layout.removeView(reserve_btn);
                        }

                        FirestoreRecyclerOptions<UserModel> newOptions = new FirestoreRecyclerOptions.Builder<UserModel>()
                                .setQuery(query, UserModel.class).build();

                        if (adapter == null || !newOptions.equals(options)) {
                            options = newOptions;
                            adapter = new SearchUserRecyclerAdapter(options, getApplicationContext());
                            userlist_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            userlist_recycler.setAdapter(adapter);
                            adapter.startListening();
                        }
                    }
                });
    }

    private static Task<Boolean> addToExistingBooking(int _timeSlot) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, CalendarUtils.formattedDate(CalendarUtils.selectedDate)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        boolean found = false;
                        for (DocumentSnapshot doc : snapshots) {
                            BookingModel bookingModel = doc.toObject(BookingModel.class);
                            int timeSlot = bookingModel.getTimeSlot();
                            if (timeSlot == _timeSlot) {
                                if(bookingModel.getUserIdList().size() + 1 >= 4)
                                    bookingModel.setReserved(true);
                                ArrayList<String> users = bookingModel.getUserIdList();
                                users.add(FirebaseUtils.currentUserId());
                                ArrayList<PlayerResult> matchResults = bookingModel.getMatchResults();
                                matchResults.add(new PlayerResult(FirebaseUtils.currentUserId(), "?"));
                                doc.getReference().update(
                                                new HashMap<String, Object>() {{
                                                    put("userIdList", users);
                                                    put("matchResults", matchResults);
                                                    put("reserved", bookingModel.isReserved());
                                                }}
                                        )
                                        .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(true))
                                        .addOnFailureListener(taskCompletionSource::setException);
                                found = true;
                                break;  // Exit loop once the time slot is found and updated
                            }
                        }
                        if (!found) {
                            taskCompletionSource.setResult(false);
                        }
                    } else {
                        taskCompletionSource.setException(task.getException());
                    }
                });
        return taskCompletionSource.getTask();
    }

    void book(){
        addToExistingBooking(CalendarUtils.mapTimeSlot(TIMESLOT)).addOnSuccessListener(isFound -> {
            if (isFound) {
                // Operazione di aggiornamento riuscita
                Log.d("BOOKING", "Updated Existing Booking");
            } else {
                // Nessun documento trovato con il time slot specificato
                BookingModel bookingModel= new BookingModel(FIELD_NAME, CalendarUtils.selectedDate, CalendarUtils.mapTimeSlot(TIMESLOT), FirebaseUtils.currentUserId());
                String userList = "";
                for(String user : bookingModel.getUserIdList())
                    userList += user + " ";
                Log.d("BOOKING", "bookignModel:\nfieldId -> " + bookingModel.getFieldId() + "\nDate -> " + CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate())) + "\ntimeSlot -> " + bookingModel.getTimeSlot() + "\nusers -> " + userList);

                FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, CalendarUtils.formattedDate(CalendarUtils.selectedDate)).add(bookingModel)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firestore", "Error adding document", e);
                        });
            }
            AndroidUtils.showToast(this, "Field Booked");
            finish();
        });
    }
}