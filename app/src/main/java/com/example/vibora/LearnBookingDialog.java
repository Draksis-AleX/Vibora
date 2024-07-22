package com.example.vibora;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibora.model.BookingModel;
import com.example.vibora.model.LessonModel;
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

public class LearnBookingDialog extends AppCompatActivity {
    static String FIELD_NAME;
    static String DATE;
    String TIMESLOT;
    TextView field_txt, date_txt, timeslot_txt;
    Button confirm_btn, cancel_btn;
    EditText user_edit_text;
    static UserModel teacher_userModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learn_booking_dialog);

        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        DATE = getIntent().getStringExtra("DATE");
        TIMESLOT = getIntent().getStringExtra("TIMESLOT");

        initViews();

        confirm_btn.setOnClickListener(v -> {
            reserveField();
        });

        cancel_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        field_txt = findViewById(R.id.field_txt);
        date_txt = findViewById(R.id.date_txt);
        timeslot_txt = findViewById(R.id.timeslot_txt);
        confirm_btn = findViewById(R.id.confirm_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        user_edit_text = findViewById(R.id.username_edit);

        field_txt.setText("Booking Lesson Slot on " + FIELD_NAME);
        date_txt.setText(DATE);
        timeslot_txt.setText(TIMESLOT);
    }

    //==============================================================================================

    private void reserveField() {
        String username = user_edit_text.getText().toString();
        if(TextUtils.isEmpty(username)){
            user_edit_text.setError("You Must Enter Username");
            return;
        }

        FirebaseUtils.allUserCollectionReference().whereEqualTo("username",username).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(task.isSuccessful()){
                            if(!querySnapshot.isEmpty()){
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String userId = document.getId();
                                    teacher_userModel = document.toObject(UserModel.class);
                                }

                                addToExistingBooking(CalendarUtils.mapTimeSlot(TIMESLOT)).addOnSuccessListener(isFound -> {
                                    if (isFound) {
                                        // Operazione di aggiornamento riuscita
                                        Log.d("BOOKING", "Updated Existing Booking");
                                    } else {
                                        // Nessun documento trovato con il time slot specificato
                                        ArrayList<String> user_list = new ArrayList<>();
                                        user_list.add(teacher_userModel.getUserId());
                                        ArrayList<PlayerResult> matchResult = new ArrayList<>();
                                        BookingModel bookingModel = new BookingModel(FIELD_NAME, CalendarUtils.selectedDate, CalendarUtils.mapTimeSlot(TIMESLOT), user_list, matchResult);
                                        String userList = "";
                                        for(String user : bookingModel.getUserIdList())
                                            userList += user + " ";
                                        Log.d("BOOKING", "bookignModel:\nfieldId -> " + bookingModel.getFieldId() + "\nDate -> " + CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate())) + "\ntimeSlot -> " + bookingModel.getTimeSlot() + "\nusers -> " + userList);

                                        FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, CalendarUtils.formattedDate(CalendarUtils.selectedDate)).add(bookingModel)
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                    addLessonToFirestore();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("Firestore", "Error adding document", e);
                                                });
                                    }
                                    AndroidUtils.showToast(LearnBookingDialog.this, "Field Booked");
                                    finish();
                                });

                            }
                            else {
                                AndroidUtils.showToast(getApplicationContext(), "No User found with this Username");
                                user_edit_text.setError("Username NOT found");
                            }
                        }
                        else AndroidUtils.showToast(getApplicationContext(), "Error getting user data");
                    }
                });
    }

    private void addLessonToFirestore() {
        LessonModel lessonModel = new LessonModel(FIELD_NAME, CalendarUtils.convertFromLocalDateToTimestamp(CalendarUtils.selectedDate), CalendarUtils.mapTimeSlot(TIMESLOT), teacher_userModel.getUserId());
        FirebaseUtils.allLessonsCollectionReference().add(lessonModel);
    }

    private static Task<Boolean> addToExistingBooking(int _timeSlot) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, DATE).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        boolean found = false;
                        for (DocumentSnapshot doc : snapshots) {
                            BookingModel bookingModel = doc.toObject(BookingModel.class);
                            int timeSlot = bookingModel.getTimeSlot();
                            if (timeSlot == _timeSlot) {
                                bookingModel.setReserved(true);
                                ArrayList<String> users = new ArrayList<>();
                                users.add(teacher_userModel.getUserId());
                                ArrayList<PlayerResult> matchResults = new ArrayList<>();
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
}