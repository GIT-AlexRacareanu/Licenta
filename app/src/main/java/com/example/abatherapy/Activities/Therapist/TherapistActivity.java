package com.example.abatherapy.Activities.Therapist;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TherapistActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.therapist_main_activity);

        user = (User) getIntent().getSerializableExtra("user");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadFragment(TherapistHomeFragment.newInstance(user));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected;


            if (id == R.id.nav_home) {
                selected = TherapistHomeFragment.newInstance(user);
            } else if (id == R.id.nav_appointments) {
                selected = TherapistAppointmentsFragment.newInstance(user);
            } else if (id == R.id.nav_profile) {
                selected = TherapistProfileFragment.newInstance(user);
            } else {
                return false;
            }

            replaceFragment(selected);
            return true;
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
