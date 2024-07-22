package com.example.vibora.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.R;
import com.example.vibora.model.LessonModel;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.LessonViewHolder> {
    Context context;
    ArrayList<LessonModel> lessons_list;
    OnLessonClickListener listener;

    public LessonsAdapter(Context context, ArrayList<LessonModel> lessons_list, OnLessonClickListener listener) {
        this.context = context;
        this.lessons_list = lessons_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lesson_recycler_row, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        LessonModel lessonModel = lessons_list.get(position);
        holder.bind(lessonModel);
    }

    @Override
    public int getItemCount() {
        return lessons_list.size();
    }

    public interface OnLessonClickListener{
        void OnLessonClick(LessonModel lessonModel, String teacher_username);
    }

    public void updateAdapterList(ArrayList<LessonModel> lessons_list){
        this.lessons_list = lessons_list;
        notifyDataSetChanged();
    }

    public class LessonViewHolder extends RecyclerView.ViewHolder{
        AppCompatTextView date_txt, timeslot_txt, teacher_txt, field_txt;
        View status_bar;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            date_txt = itemView.findViewById(R.id.date_txt);
            timeslot_txt = itemView.findViewById(R.id.timeslot_txt);
            teacher_txt = itemView.findViewById(R.id.teacher_txt);
            status_bar = itemView.findViewById(R.id.status_indicator);
            field_txt = itemView.findViewById(R.id.field_txt);
        }

        public void bind(LessonModel lessonModel) {
            date_txt.setText(CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(lessonModel.getDate())));
            timeslot_txt.setText(CalendarUtils.mapIndexToTimeSlot(lessonModel.getTimeslot()));
            field_txt.setText("Field: " + lessonModel.getField_name());
            Log.d("LessonsAdapter", "userId -> " + lessonModel.getTeacherId());
            final UserModel[] userModel = new UserModel[1];
            String userId;
            if(lessonModel.getTeacherId().equals(FirebaseUtils.currentUserId()) && lessonModel.getUserId() != null)
                userId = lessonModel.getUserId();
            else userId = lessonModel.getTeacherId();
            FirebaseUtils.getUserDetails(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot doc = task.getResult();
                                userModel[0] = doc.toObject(UserModel.class);
                                if(lessonModel.getTeacherId().equals(FirebaseUtils.currentUserId()) && lessonModel.getUserId() != null)
                                    teacher_txt.setText("Student: @" + userModel[0].getUsername());
                                else
                                    teacher_txt.setText("Teacher: @" + userModel[0].getUsername());
                            }
                            else teacher_txt.setText("Teacher: Not Found");
                        }
                    });
            if(lessonModel.isBooked()){
                status_bar.setBackgroundColor(context.getResources().getColor(R.color.red));
                itemView.setBackgroundTintList(context.getResources().getColorStateList(R.color.light_gray));
            }
            else{
                status_bar.setBackgroundColor(context.getResources().getColor(R.color.main_green));
                itemView.setOnClickListener(v -> listener.OnLessonClick(lessonModel, userModel[0].getUsername()));
            }
        }
    }
}
