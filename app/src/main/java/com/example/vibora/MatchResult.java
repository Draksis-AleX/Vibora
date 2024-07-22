package com.example.vibora;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.UserMatchResultAdapter;
import com.example.vibora.model.BookingModel;
import com.example.vibora.model.PlayerResult;
import com.example.vibora.model.UserMatchResultModel;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MatchResult extends AppCompatActivity {
    static String FIELD_NAME, DATE, TIMESLOT;
    TextView date_text, timeslot_text, skillpoints_txt;
    ImageView cup_img;
    RecyclerView userlist_recycler;
    UserMatchResultAdapter adapter;
    Button won_btn, lose_btn, back_btn;
    ArrayList<UserMatchResultModel> userMatchResultModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.main_green));
        setContentView(R.layout.activity_match_result);

        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        DATE = getIntent().getStringExtra("DATE");
        TIMESLOT = getIntent().getStringExtra("TIMESLOT");

        initViews();
        setupUserRecyclerView();

        back_btn.setOnClickListener(v -> {
            finish();
        });

        won_btn.setOnClickListener(v -> {
            setMatchResult("Won");
            updateSkillRating(200);
        });

        lose_btn.setOnClickListener(v -> {
            setMatchResult("Lose");
            updateSkillRating(-200);
        });
    }


    @SuppressLint("DefaultLocale")
    private void initViews() {
        date_text = findViewById(R.id.date_txt);
        date_text.setText(DATE);
        timeslot_text = findViewById(R.id.timeslot_txt);
        timeslot_text.setText(TIMESLOT);
        skillpoints_txt = findViewById(R.id.skillpoints_txt);
        skillpoints_txt.setText(String.format("%04d", FirebaseUtils.currentUserModel.getSkill_rating()));

        userlist_recycler = findViewById(R.id.userlist_recycler);
        won_btn = findViewById(R.id.won_btn);
        lose_btn = findViewById(R.id.lose_btn);
        back_btn = findViewById(R.id.back_btn);
        cup_img = findViewById(R.id.cup_img);
    }

    //==============================================================================================

    private void updateSkillRating(int x) {
        FirebaseUtils.currentUserDetails().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel userModel = documentSnapshot.toObject(UserModel.class);
                userModel.setSkill_rating(userModel.getSkill_rating() + x);
                if(userModel.getSkill_rating() < 0) userModel.setSkill_rating(0);
                FirebaseUtils.currentUserModel = userModel;
                documentSnapshot.getReference().set(userModel);
                if (userModel != null) {
                    skillpoints_txt.setText(String.format("%04d", userModel.getSkill_rating()));
                }
            }
        });
    }

    private void setMatchResult(String result) {
        FirebaseUtils.dailyBookingsCollectionReference(FIELD_NAME, DATE).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        BookingModel bookingModel = null;
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            bookingModel = doc.toObject(BookingModel.class);
                            if(CalendarUtils.mapIndexToTimeSlot(bookingModel.getTimeSlot()).equals(TIMESLOT)){
                                ArrayList<PlayerResult> matchResults = bookingModel.getMatchResults();
                                for (int i = 0; i < matchResults.size(); i++){
                                    PlayerResult playerResult = matchResults.get(i);
                                    if(playerResult.getPlayerId().equals(FirebaseUtils.currentUserId())){
                                        playerResult.setResult(result);
                                        matchResults.set(i, playerResult);
                                        break;
                                    }
                                }
                                doc.getReference().update("matchResults", matchResults);
                                setupUserRecyclerView();
                                break;
                            }
                        }
                    }
                });
    }

    private void setupUserRecyclerView() {
        disableMatchResultInterface();
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

                        userMatchResultModels = new ArrayList<>();
                        int index = 0;
                        final int[] remaining_calls = {bookingModel.getUserIdList().size()};
                        for(String userId : bookingModel.getUserIdList()){
                            String result = bookingModel.getMatchResults().get(index).getResult();
                            Log.d("ADAPTER_ARRAY", userId +" result -> " + result);

                            if(userId.equals(FirebaseUtils.currentUserId()))
                                if(result.equals("?") && bookingModel.getUserIdList().size() >= 4) enableMatchResultInterface();

                            FirebaseUtils.getUsernameFromId(userId).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String username = task.getResult();
                                    if (username != null) Log.d("Username", username);
                                    else Log.d("Username", "Username not found or error occurred");

                                    UserMatchResultModel new_user = new UserMatchResultModel(userId, username, result);
                                    userMatchResultModels.add(new_user);
                                } else {
                                    Log.d("Username", "Error occurred: " + task.getException().getMessage());
                                }

                                remaining_calls[0]--;
                                if(remaining_calls[0] == 0){
                                    adapter = new UserMatchResultAdapter(getApplicationContext(), userMatchResultModels);
                                    userlist_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    userlist_recycler.setAdapter(adapter);
                                }
                            });
                            index++;
                        }
                    }
                });
    }

    private void disableMatchResultInterface(){
        won_btn.setEnabled(false);
        won_btn.setBackgroundColor(getResources().getColor(R.color.separator_gray));
        lose_btn.setEnabled(false);
        lose_btn.setBackgroundColor(getResources().getColor(R.color.separator_gray));
    }

    private void enableMatchResultInterface(){
        won_btn.setEnabled(true);
        won_btn.setBackgroundColor(getResources().getColor(R.color.main_green));
        lose_btn.setEnabled(true);
        lose_btn.setBackgroundColor(getResources().getColor(R.color.red));
    }
}