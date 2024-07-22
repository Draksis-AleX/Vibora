package com.example.vibora.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.OtherProfile;
import com.example.vibora.R;
import com.example.vibora.model.UserMatchResultModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserMatchResultAdapter extends RecyclerView.Adapter<UserMatchResultAdapter.UserModelMatchResultViewHolder> {
    Context context;
    ArrayList<UserMatchResultModel> user_list;

    public UserMatchResultAdapter(Context context, ArrayList<UserMatchResultModel> user_list) {
        this.context = context;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public UserModelMatchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_match_result_row, parent, false);
        return new UserModelMatchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserModelMatchResultViewHolder holder, int position) {

        UserMatchResultModel user = user_list.get(position);

        holder.bind(user);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherProfile.class);
            intent.putExtra(OtherProfile.EXTRA_USER_ID, user_list.get(position).getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return user_list.size();
    }

    public class UserModelMatchResultViewHolder extends RecyclerView.ViewHolder{
        AppCompatTextView result_text;
        TextView usernameText;
        CircleImageView profilePic;

        public UserModelMatchResultViewHolder(@NonNull View itemView){
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text_search_result);
            profilePic = itemView.findViewById(R.id.profile_image_search_result);
            result_text = itemView.findViewById(R.id.result_text);
        }

        public void bind(UserMatchResultModel user) {
            if (user.getUserId().equals(FirebaseUtils.currentUserId())) usernameText.setText(user.getUsername() + " (Me)");
            else usernameText.setText(user.getUsername());

            FirebaseUtils.getUserProfilePicStorageRef(user.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Uri uri = task.getResult();
                            AndroidUtils.setProfilePic(context,uri,profilePic);
                        }
                    });

            result_text.setText(user.getResult());
            if(user.getResult().equals("Won")) result_text.setBackgroundTintList(context.getResources().getColorStateList(R.color.main_green));
            else if(user.getResult().equals("Lose")) result_text.setBackgroundTintList(context.getResources().getColorStateList(R.color.red));
            else result_text.setBackgroundTintList(context.getResources().getColorStateList(R.color.separator_gray));
        }
    }

}
