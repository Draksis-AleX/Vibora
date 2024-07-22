package com.example.vibora;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.UserReserveRowAdapter;
import com.example.vibora.model.BookingModel;
import com.example.vibora.model.PlayerResult;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ReserveDialog extends AppCompatActivity implements UserReserveRowAdapter.OnDeleteUserClickListener{
    static String FIELD_NAME, TIMESLOT;
    Button add_btn, close_btn;
    EditText user_edit_text;
    RecyclerView user_list_recycler;
    static ArrayList<UserModel> user_list;
    UserReserveRowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserve_dialog);

        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        TIMESLOT = getIntent().getStringExtra("TIMESLOT");

        initWidgets();
        setUserRecycler();

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_list.size() < 4)
                    addUser();
                else
                    completeReservation();
            }
        });
    }

    private void completeReservation() {
        addToExistingBooking(CalendarUtils.mapTimeSlot(TIMESLOT)).addOnSuccessListener(isFound -> {
            if (isFound) {
                // Operazione di aggiornamento riuscita
                Log.d("BOOKING", "Updated Existing Booking");
            } else {
                // Nessun documento trovato con il time slot specificato
                ArrayList<String> user_booking_list = new ArrayList<>();
                ArrayList<PlayerResult> matchResults = new ArrayList<>();
                for(UserModel userModel : user_list){
                    matchResults.add(new PlayerResult(userModel.getUserId(), "?"));
                    user_booking_list.add(userModel.getUserId());
                }
                BookingModel bookingModel= new BookingModel(FIELD_NAME, CalendarUtils.selectedDate, CalendarUtils.mapTimeSlot(TIMESLOT), user_booking_list, matchResults);
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
                                ArrayList<String> user_booking_list = new ArrayList<>();
                                ArrayList<PlayerResult> matchResults = new ArrayList<>();
                                for(UserModel userModel : user_list){
                                    matchResults.add(new PlayerResult(userModel.getUserId(), "?"));
                                    user_booking_list.add(userModel.getUserId());
                                }
                                bookingModel.setReserved(true);
                                doc.getReference().update(
                                                new HashMap<String, Object>() {{
                                                    put("userIdList", user_booking_list);
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

    private void addUser() {
        String username = user_edit_text.getText().toString();
        if(TextUtils.isEmpty(username)){
            user_edit_text.setError("You Must Enter Username");
            return;
        }

        add_btn.setEnabled(false);
        add_btn.setBackgroundColor(getApplicationContext().getColor(R.color.separator_gray));

        FirebaseUtils.allUserCollectionReference().whereEqualTo("username",username).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(task.isSuccessful()){
                            if(!querySnapshot.isEmpty()){
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String userId = document.getId();
                                    UserModel userModel = document.toObject(UserModel.class);
                                    user_list.add(userModel);
                                    adapter.updateUserList(user_list);
                                    if(user_list.size() >= 4){
                                        add_btn.setText("Complete");
                                        AndroidUtils.hideKeyboardFrom(ReserveDialog.this, user_edit_text);
                                    }
                                }
                            }
                            else {
                                AndroidUtils.showToast(getApplicationContext(), "No User found with this Username");
                                user_edit_text.setError("Username NOT found");
                            }
                        }
                        else AndroidUtils.showToast(getApplicationContext(), "Error getting user data");
                        add_btn.setEnabled(true);
                        add_btn.setBackgroundColor(getApplicationContext().getColor(R.color.main_green));
                        user_edit_text.setText("");
                    }
                });
    }

    private void setUserRecycler() {
        user_list = new ArrayList<>();
        UserModel currentUserModel = FirebaseUtils.currentUserModel;
        user_list.add(currentUserModel);
        adapter = new UserReserveRowAdapter(getApplicationContext(), user_list, ReserveDialog.this);
        user_list_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        user_list_recycler.setAdapter(adapter);
    }

    private void initWidgets() {
        add_btn = findViewById(R.id.add_btn);
        close_btn = findViewById(R.id.close_btn);
        user_list_recycler = findViewById(R.id.user_list_recycler);
        user_edit_text = findViewById(R.id.username_edit);
    }

    @Override
    public void onDeleteClickListener(UserModel userModel) {
        int index = user_list.indexOf(userModel);
        if(index != -1){
            user_list.remove(index);
            adapter.updateUserList(user_list);
            add_btn.setText("Add");
        }
    }
}