package com.example.vibora.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.OtherProfile;
import com.example.vibora.R;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder userModelViewHolder, int i, @NonNull UserModel userModel) {
        userModelViewHolder.usernameText.setText(userModel.getUsername());
        if(userModel.getUserId().equals(FirebaseUtils.currentUserId())){
            userModelViewHolder.usernameText.setText(userModel.getUsername() + " (Me)");
        }
        userModelViewHolder.rankingpoints_txt.setText(String.format("%04d", userModel.getSkill_rating()));
        userModelViewHolder.cup_img.setImageDrawable(context.getResources().getDrawable(R.drawable.cup_icon));

        FirebaseUtils.getUserProfilePicStorageRef(userModel.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Uri uri = task.getResult();
                        AndroidUtils.setProfilePic(context,uri,userModelViewHolder.profilePic);
                    }
                });

        userModelViewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherProfile.class);
            intent.putExtra(OtherProfile.EXTRA_USER_ID, userModel.getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText, rankingpoints_txt;
        CircleImageView profilePic;
        ImageView cup_img;

        public UserModelViewHolder(@NonNull View itemView){
            super(itemView);
            rankingpoints_txt = itemView.findViewById(R.id.rankingpoints_text);
            usernameText = itemView.findViewById(R.id.username_text_search_result);
            profilePic = itemView.findViewById(R.id.profile_image_search_result);
            cup_img = itemView.findViewById(R.id.cup_img);
        }
    }
}
