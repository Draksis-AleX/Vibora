package com.example.vibora.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.R;
import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserReserveRowAdapter extends RecyclerView.Adapter<UserReserveRowAdapter.UserReserveViewHolder> {
    Context context;
    ArrayList<UserModel> user_list;
    OnDeleteUserClickListener listener;

    public UserReserveRowAdapter(Context context, ArrayList<UserModel> user_list, OnDeleteUserClickListener listener) {
        this.context = context;
        this.user_list = user_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserReserveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_reserve_row, parent, false);
        return new UserReserveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReserveViewHolder holder, int position) {
        UserModel userModel = user_list.get(position);
        holder.bind(userModel);
    }

    @Override
    public int getItemCount() {
        return user_list.size();
    }

    public void updateUserList(ArrayList<UserModel> user_list){
        this.user_list = user_list;
        notifyDataSetChanged();
    }

    public interface OnDeleteUserClickListener {
        void onDeleteClickListener(UserModel userModel);
    }

    public class UserReserveViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText, rankingpoints_txt;
        CircleImageView profilePic;
        ImageView cup_img;
        ImageButton delete_img_btn;

        public UserReserveViewHolder(@NonNull View itemView) {
            super(itemView);
            rankingpoints_txt = itemView.findViewById(R.id.rankingpoints_text);
            usernameText = itemView.findViewById(R.id.username_text_search_result);
            profilePic = itemView.findViewById(R.id.profile_image_search_result);
            cup_img = itemView.findViewById(R.id.cup_img);
            delete_img_btn = itemView.findViewById(R.id.delete_img_btn);
        }

        public void bind(UserModel userModel) {
            usernameText.setText(userModel.getUsername());
            rankingpoints_txt.setText(String.format("%04d", userModel.getSkill_rating()));
            if(userModel.getUserId().equals(FirebaseUtils.currentUserId())){
                delete_img_btn.setEnabled(false);
                delete_img_btn.setColorFilter(context.getResources().getColor(R.color.separator_gray));
            }

            FirebaseUtils.getUserProfilePicStorageRef(userModel.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Uri uri = task.getResult();
                            AndroidUtils.setProfilePic(context,uri,profilePic);
                        }
                    });

            delete_img_btn.setOnClickListener(v -> listener.onDeleteClickListener(userModel));
        }
    }
}
