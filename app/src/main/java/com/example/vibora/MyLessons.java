package com.example.vibora;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.LessonsAdapter;
import com.example.vibora.model.LessonModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class MyLessons extends AppCompatActivity implements LessonsAdapter.OnLessonClickListener{
    RecyclerView lessons_student_recycler, lessons_teacher_recycler;
    ArrayList<LessonModel> lessons_student_list, lessons_teacher_list;
    LessonsAdapter lessons_student_adapter, lessons_teacher_adapter;
    Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_green));
        setContentView(R.layout.activity_my_lessons);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        lessons_student_list = new ArrayList<>();
        lessons_student_adapter = new LessonsAdapter(getApplicationContext(), lessons_student_list, this);
        lessons_student_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lessons_student_recycler.setAdapter(lessons_student_adapter);
        lessons_teacher_list = new ArrayList<>();
        lessons_teacher_adapter = new LessonsAdapter(getApplicationContext(), lessons_teacher_list, this);
        lessons_teacher_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lessons_teacher_recycler.setAdapter(lessons_teacher_adapter);

        setupLessonsRecyclers();

        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        lessons_student_recycler = findViewById(R.id.lessons_student_recycler);
        lessons_teacher_recycler = findViewById(R.id.lessons_teacher_recycler);
        back_btn = findViewById(R.id.back_btn);
    }

    //==============================================================================================

    private void setupLessonsRecyclers() {
        FirebaseUtils.allLessonsCollectionReference().whereEqualTo("teacherId", FirebaseUtils.currentUserId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            LessonModel lessonModel = doc.toObject(LessonModel.class);
                            Log.d("MyLessons", "teacherId -> " + lessonModel.getTeacherId() + " , ispast -> " + isPast(lessonModel) + " , isbooked -> " + lessonModel.isBooked());
                            if(!isPast(lessonModel) && lessonModel.isBooked()){
                                lessons_teacher_list.add(lessonModel);
                            }
                        }

                        Log.d("MyLessons", "teacher list size -> " + lessons_teacher_list.size());
                        lessons_teacher_adapter.updateAdapterList(lessons_teacher_list);
                    }
                });

        FirebaseUtils.allLessonsCollectionReference().whereEqualTo("userId", FirebaseUtils.currentUserId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            LessonModel lessonModel = doc.toObject(LessonModel.class);
                            Log.d("MyLessons", "userId -> " + lessonModel.getUserId() + " , ispast -> " + isPast(lessonModel) + " , isbooked -> " + lessonModel.isBooked());
                            if(!isPast(lessonModel) && lessonModel.isBooked()){
                                lessons_student_list.add(lessonModel);
                            }
                        }

                        lessons_student_adapter.updateAdapterList(lessons_student_list);
                    }
                });
    }

    private boolean isPast(LessonModel lessonModel) {
        if(CalendarUtils.convertFromTimestampToLocalDate(lessonModel.getDate()).isBefore(LocalDate.now())) return true;
        if(CalendarUtils.convertFromTimestampToLocalDate(lessonModel.getDate()).isEqual(LocalDate.now())){
            LocalTime slotStartTime = LocalTime.of(9, 0).plusMinutes(90 * lessonModel.getTimeslot());
            if(slotStartTime.isBefore(LocalTime.now())) return true;
        }
        return false;
    }

    @Override
    public void OnLessonClick(LessonModel lessonModel, String teacher_username) {
    }
}