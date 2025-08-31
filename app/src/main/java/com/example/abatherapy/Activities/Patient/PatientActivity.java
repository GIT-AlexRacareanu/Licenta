package com.example.abatherapy.Activities.Patient;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_main_activity);

        user = (User) getIntent().getSerializableExtra("user");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadFragment(PatientHomeFragment.newInstance(user));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected;


            if (id == R.id.nav_home) {
                selected = PatientHomeFragment.newInstance(user);
            } else if (id == R.id.nav_appointments) {
                selected = PatientAppointmentsFragment.newInstance(user);
            } else if (id == R.id.nav_profile) {
                selected = PatientProfileFragment.newInstance(user);
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
