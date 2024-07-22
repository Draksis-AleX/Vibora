package com.example.vibora;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vibora.model.UserModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {
    TextView full_name, full_name2, username, username2, email, ranking_score;
    Button logoutBtn;
    CircleImageView profile_image;
    ProgressBar progressBar;

    UserModel currentUser;
    ActivityResultLauncher<Intent> imagePickLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Uri[] selectedImageUri = new Uri[1];
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null){
                            selectedImageUri[0] = data.getData();
                            currentUser.setProfilePic(selectedImageUri[0].toString());
                            AndroidUtils.setProfilePic(getContext(), selectedImageUri[0], profile_image);

                            if(selectedImageUri[0] != null) {
                                progressBar.setVisibility(View.VISIBLE);
                                logoutBtn.setEnabled(false);
                                disableBottomNavigationButtons();
                                FirebaseUtils.getCurrentProfilePicStorageRef().putFile(selectedImageUri[0])
                                        .addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                AndroidUtils.showToast(getContext(), "Profile Pic Updated Successfully");
                                            }
                                            else{
                                                AndroidUtils.showToast(getContext(), "Profile Pic update failed");
                                            }
                                            progressBar.setVisibility(View.GONE);
                                            logoutBtn.setEnabled(true);
                                            enableBottomNavigationButtons();
                                        });
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);

        logoutBtn.setOnClickListener(v -> {
            FirebaseUtils.signOut();
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        profile_image.setOnClickListener(v -> {
            ImagePicker.with(ProfileFragment.this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUserData();
    }

    private void initViews(View view) {
        full_name = view.findViewById(R.id.profile_full_name);
        full_name2 = view.findViewById(R.id.profile_full_name_2);
        username = view.findViewById(R.id.profile_username);
        username2 = view.findViewById(R.id.profile_username_2);
        email = view.findViewById(R.id.profile_email);
        logoutBtn = view.findViewById(R.id.profile_logout_btn);
        profile_image = view.findViewById(R.id.profile_image);
        ranking_score = view.findViewById(R.id.profile_ranking_score);
        progressBar = view.findViewById(R.id.progressBar);
    }

    //==============================================================================================

    void getUserData(){
        currentUser = FirebaseUtils.currentUserModel;

        if(currentUser.getProfilePic() != null) AndroidUtils.setProfilePic(getContext(),Uri.parse(currentUser.getProfilePic()),profile_image);
        else{
            FirebaseUtils.getCurrentProfilePicStorageRef().getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Uri uri = task.getResult();
                            if(isAdded() && getActivity() != null){
                                AndroidUtils.setProfilePic(getContext(),uri,profile_image);
                                currentUser.setProfilePic(uri.toString());
                            }
                        }
                    });
        }

        full_name.setText(currentUser.getFull_name());
        full_name2.setText(currentUser.getFull_name());
        String _username = "@" + currentUser.getUsername();
        username.setText(_username);
        username2.setText(_username);
        email.setText(currentUser.getEmail());
        ranking_score.setText(String.format("%d", currentUser.getSkill_rating()));
    }

    private void disableBottomNavigationButtons() {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setEnabled(false);
        }
    }

    private void enableBottomNavigationButtons() {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setEnabled(true);
        }
    }
}