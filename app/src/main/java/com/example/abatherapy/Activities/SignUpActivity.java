package com.example.abatherapy.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abatherapy.Activities.Evaluator.EvaluatorActivity;
import com.example.abatherapy.Activities.Patient.PatientActivity;
import com.example.abatherapy.Activities.Therapist.TherapistActivity;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private EditText editFirstName, editLastName, editPhone, editAddress, editDescriere, editExperienta, editSpecializare, editEmail, editPassword, editConfirmPassword;
    private TextView textLoginLink;
    private Button signUpButton;
    private Spinner spinnerRole;
    private MaterialSwitch isEvaluatorSwitch;

    private User newUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        // Initialize views
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editDescriere = findViewById(R.id.editDescriere);
        editExperienta = findViewById(R.id.editExperienta);
        editSpecializare = findViewById(R.id.editSpecializare);
        textLoginLink = findViewById(R.id.textLoginLink);
        signUpButton = findViewById(R.id.signUpButton);
        spinnerRole = findViewById(R.id.spinnerRole);
        isEvaluatorSwitch = findViewById(R.id.switchEvaluator);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Hide Terapeut-specific fields initially
        toggleTerapeutFields(false);

        // Show Terapeut-specific fields when selected
        spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String role = spinnerRole.getSelectedItem().toString();
                toggleTerapeutFields(role.equalsIgnoreCase("Terapeut"));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                toggleTerapeutFields(false);
            }
        });

        // Sign-up button click
        signUpButton.setOnClickListener(view -> attemptSignUp());

        // Login link click
        textLoginLink.setOnClickListener(view -> {
            startActivity(new Intent(this, LogInActivity.class));
            finish();
        });
    }

    private void toggleTerapeutFields(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        editDescriere.setVisibility(visibility);
        editExperienta.setVisibility(visibility);
        editSpecializare.setVisibility(visibility);
        isEvaluatorSwitch.setVisibility(visibility);
    }

    private void attemptSignUp() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String descriere = editDescriere.getText().toString().trim();
        String experienta = editExperienta.getText().toString().trim();
        String specializare = editSpecializare.getText().toString().trim();
        boolean evaluator = isEvaluatorSwitch.isChecked();
        String role = spinnerRole.getSelectedItem().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();

                        newUser = new User(
                                address,
                                role.equalsIgnoreCase("Terapeut") ? descriere : "",
                                email,
                                role.equalsIgnoreCase("Terapeut") && evaluator,
                                role.equalsIgnoreCase("Terapeut") ? experienta : "",
                                firstName,
                                lastName,
                                phone,
                                role,
                                role.equalsIgnoreCase("Terapeut") ? specializare : "",
                                uid
                        );

                        saveUserToFirestore(newUser);
                    } else {
                        Toast.makeText(this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    navigateAfterSignUp(user);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void navigateAfterSignUp(User user) {
        Intent intent;
        switch (user.getRole()) {
            case "Pacient":
                intent = new Intent(this, PatientActivity.class);
                break;
            case "Terapeut":
                if (user.isEvaluator()) {
                    intent = new Intent(this, EvaluatorActivity.class);
                } else {
                    intent = new Intent(this, TherapistActivity.class);
                }
                break;
            default:
                return;
        }
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }
}
