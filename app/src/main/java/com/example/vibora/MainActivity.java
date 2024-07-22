package com.example.vibora;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.vibora.utils.FirebaseUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottom_nav_view;
    private FrameLayout frame_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottom_nav_view = findViewById(R.id.bottom_nav);
        frame_layout = findViewById(R.id.frame_layout);

        bottom_nav_view.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemID = menuItem.getItemId();

                if(itemID == R.id.nav_home){

                    loadFragment(new HomeFragment(), true);

                }else if(itemID == R.id.nav_search){

                    loadFragment(new SearchFragment(), true);

                }else if(itemID == R.id.nav_learn){

                    loadFragment(new LearnFragment(), true);

                }else if(itemID == R.id.nav_profile){
                    if(FirebaseUtils.currentUserModel.getIsAdmin() == 1){
                            loadFragment(new ProfileFragmentAdmin(), true);
                    }
                    else loadFragment(new ProfileFragment(), true);
                }
                return true;
            }
        });

        loadFragment(new HomeFragment(), false);
    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(!isAppInitialized){
            fragmentTransaction.add(R.id.frame_layout, fragment);
        } else fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}