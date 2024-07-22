package com.example.vibora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibora.model.LessonModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class BookLessonDialog extends AppCompatActivity {
    static String FIELD_NAME, TEACHER_USERNAME;
    static Timestamp DATE;
    static int TIMESLOT;

    TextView field_txt, date_txt, timeslot_txt, teacher_txt;
    Button confirm_btn, cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_lesson_dialog);

        getDataFromIntent();
        initViews();

        confirm_btn.setOnClickListener(v -> {
            bookLesson();
        });

        cancel_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        field_txt = findViewById(R.id.field_txt);
        date_txt = findViewById(R.id.date_txt);
        timeslot_txt = findViewById(R.id.timeslot_txt);
        teacher_txt = findViewById(R.id.teacher_txt);
        confirm_btn = findViewById(R.id.confirm_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        field_txt.setText("Booking Lesson on " + FIELD_NAME);
        date_txt.setText(CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(DATE)));
        timeslot_txt.setText(CalendarUtils.mapIndexToTimeSlot(TIMESLOT));
        teacher_txt.setText("Teacher: @" + TEACHER_USERNAME);
    }

    private void getDataFromIntent() {
        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        TEACHER_USERNAME = getIntent().getStringExtra("TEACHER_USERNAME");
        DATE = getIntent().getParcelableExtra("DATE");
        TIMESLOT = getIntent().getIntExtra("TIMESLOT", -1);
    }

    //==============================================================================================

    private void bookLesson() {
        FirebaseUtils.allLessonsCollectionReference()
                .whereEqualTo("date", DATE)
                .whereEqualTo("timeslot", TIMESLOT)
                .whereEqualTo("field_name", FIELD_NAME)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            LessonModel lesson = document.toObject(LessonModel.class);
                            System.out.println(lesson);
                            document.getReference().update(new HashMap<String, Object>() {{
                                put("userId", FirebaseUtils.currentUserId());
                                put("booked", true);
                            }});
                        }
                        AndroidUtils.showToast(BookLessonDialog.this, "Lesson Booked");
                    } else {
                        AndroidUtils.showToast(BookLessonDialog.this, "Error: Lesson Not Booked");
                    }
                    finish();
                });
    }
}