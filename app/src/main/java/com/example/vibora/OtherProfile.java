package com.example.vibora;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherProfile extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "extra_user_id";

    TextView full_name, full_name2, username, username2, ranking_score;
    ImageButton backBtn;
    Button reportBtn, ban_btn, reset_skill_rating_btn, unban_btn;
    CircleImageView profile_image;

    UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_green));
        setContentView(R.layout.activity_other_profile);

        String userId = getIntent().getStringExtra(EXTRA_USER_ID);

        initViews();
        getUserData(userId);

        unban_btn.setOnClickListener(v -> {
            unbanUser();
        });

        ban_btn.setOnClickListener(v -> {
            banUser();
        });

        reset_skill_rating_btn.setOnClickListener(v -> {
            resetUserSkillRating();
        });

        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user == null) return;
                Intent intent = new Intent(getApplicationContext(), ReportDialog.class);
                intent.putExtra("USER_ID", user.getUserId());
                intent.putExtra("USERNAME", user.getUsername());
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        full_name = findViewById(R.id.profile_full_name);
        full_name2 = findViewById(R.id.profile_full_name_2);
        username = findViewById(R.id.profile_username);
        username2 = findViewById(R.id.profile_username_2);
        ranking_score = findViewById(R.id.profile_ranking_score);
        profile_image = findViewById(R.id.profile_image);
        backBtn = findViewById(R.id.back_button);
        reportBtn = findViewById(R.id.report_btn);
        ban_btn = findViewById(R.id.ban_btn);
        unban_btn = findViewById(R.id.unban_btn);
        reset_skill_rating_btn = findViewById(R.id.reset_skill_rating_btn);
        if(FirebaseUtils.currentUserModel.getIsAdmin() == 0){
            ViewGroup layout = (ViewGroup) ban_btn.getParent();
            if(layout!=null){
                layout.removeView(ban_btn);
                layout.removeView(reset_skill_rating_btn);
                layout.removeView(unban_btn);
            }
        }
    }

    //==============================================================================================

    private void unbanUser() {
        user.setBanned(0);
        FirebaseUtils.getUserDetails(user.getUserId()).update("isBanned", user.getIsBanned())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            AndroidUtils.showToast(getApplicationContext(), "User Unbanned");
                        }
                    }
                });
    }

    private void banUser() {
        user.setBanned(1);
        FirebaseUtils.getUserDetails(user.getUserId()).update("isBanned", user.getIsBanned())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            AndroidUtils.showToast(getApplicationContext(), "User Banned");
                        }
                    }
                });
    }

    private void resetUserSkillRating() {
        user.setSkill_rating(0);
        FirebaseUtils.getUserDetails(user.getUserId()).update("skill_rating", user.getSkill_rating())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ranking_score.setText(String.format("%04d", user.getSkill_rating()));
                            AndroidUtils.showToast(getApplicationContext(), "User Skill Score Resetted");
                        }
                    }
                });
    }

    void getUserData(String userId){
        FirebaseUtils.getUserProfilePicStorageRef(userId).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri = task.getResult();
                        if(!isFinishing() && !isDestroyed()) AndroidUtils.setProfilePic(this,uri,profile_image);
                    }
                });

        FirebaseUtils.getUserDetails(userId).get().addOnCompleteListener(task -> {
            user = task.getResult().toObject(UserModel.class);
            full_name.setText(user.getFull_name());
            full_name2.setText(user.getFull_name());
            String _username = "@" + user.getUsername();
            username.setText(_username);
            username2.setText(_username);
            ranking_score.setText(String.format("%d", user.getSkill_rating()));
        });
    }
}