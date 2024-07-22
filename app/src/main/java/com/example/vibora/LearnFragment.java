package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.vibora.adapter.LessonsAdapter;
import com.example.vibora.model.LessonModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LearnFragment extends Fragment implements LessonsAdapter.OnLessonClickListener{
    RecyclerView lessons_recycler;
    Button mylessons_btn, setlessons_btn;
    ArrayList<LessonModel> lessons_list;
    boolean isRecylerInitialized = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        initViews(view);
        setupLessonRecyler();

        setlessons_btn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SetLessons.class));
        });

        mylessons_btn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MyLessons.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isRecylerInitialized) setupLessonRecyler();
        isRecylerInitialized = true;
    }

    private void initViews(View view) {
        lessons_recycler = view.findViewById(R.id.field_recycler);
        mylessons_btn = view.findViewById(R.id.mylessons_btn);
        setlessons_btn = view.findViewById(R.id.setlessons_btn);
        if(FirebaseUtils.currentUserModel.getIsAdmin() == 0){
            ViewGroup layout = (ViewGroup) setlessons_btn.getParent();
            if(layout!=null) layout.removeView(setlessons_btn);
        }
    }

    //==============================================================================================

    private void setupLessonRecyler() {
        lessons_list = new ArrayList<>();
        FirebaseUtils.allLessonsCollectionReference().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();
                            for(DocumentSnapshot doc : querySnapshot.getDocuments()){
                                LessonModel lessonModel = doc.toObject(LessonModel.class);
                                if(!isPast(lessonModel) && !lessonModel.getTeacherId().equals(FirebaseUtils.currentUserId())) lessons_list.add(lessonModel);
                            }
                            Collections.sort(lessons_list, new LessonModelComparator());
                            LessonsAdapter adapter = new LessonsAdapter(getContext(), lessons_list, LearnFragment.this);
                            lessons_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
                            lessons_recycler.setAdapter(adapter);
                        }
                        else Log.d("LESSON RETRIEVE", "Fail.");
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

    public class LessonModelComparator implements Comparator<LessonModel> {
        @Override
        public int compare(LessonModel lesson1, LessonModel lesson2) {
            int dateComparison = lesson1.getDate().compareTo(lesson2.getDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return Integer.compare(lesson1.getTimeslot(), lesson2.getTimeslot());
        }
    }

    @Override
    public void OnLessonClick(LessonModel lessonModel, String teacher_username) {
        Intent intent = new Intent(getContext(), BookLessonDialog.class);
        intent.putExtra("FIELD_NAME", lessonModel.getField_name());
        intent.putExtra("DATE", lessonModel.getDate());
        intent.putExtra("TIMESLOT", lessonModel.getTimeslot());
        intent.putExtra("TEACHER_USERNAME", teacher_username);
        startActivity(intent);
    }
}