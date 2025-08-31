package com.example.abatherapy.Activities.Therapist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TherapistProfileFragment extends Fragment {

    private User user;
    private TextView txtName, txtEmail, txtPhone, txtAddress, txtRole;

    public static TherapistProfileFragment newInstance(User user) {
        TherapistProfileFragment fragment = new TherapistProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.therapist_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtFullName = view.findViewById(R.id.txtFullName);
        TextView txtEmail = view.findViewById(R.id.txtEmail);
        TextView txtPhone = view.findViewById(R.id.txtPhone);
        TextView txtAddress = view.findViewById(R.id.txtAddress);
        TextView txtRole = view.findViewById(R.id.txtRole);
        TextView txtSpecializare = view.findViewById(R.id.txtSpecializare);
        TextView txtExperienta = view.findViewById(R.id.txtExperienta);
        TextView txtDescriere = view.findViewById(R.id.txtDescriere);

        if (user != null) {
            txtFullName.setText(user.getFirstName() + " " + user.getLastName());
            txtEmail.setText("Email: " + user.getEmail());
            txtPhone.setText("Telefon: " + user.getPhone());
            txtAddress.setText("Adresa: " + user.getAddress());
            txtRole.setText(user.getRole());
            txtSpecializare.setText("Specializare: " + user.getSpecializare());
            txtExperienta.setText("Experienta: " + user.getExperienta());
            txtDescriere.setText("Descriere: " + user.getDescriere());
        }
    }
}
