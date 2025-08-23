package com.example.abatherapy.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abatherapy.Database.DBManager;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseDatabase database = DBManager.getDB();
    EditText editFirstName, editLastName, editPhone, editAddress, editDescriere, editExperienta, editSpecializare, editEmail, editPassword, editConfirmPassword;
    TextView textLoginLink, textTitle;
    Button signUpButton;
    Spinner spinnerRole;
    MaterialSwitch isEvaluatorSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editDescriere = findViewById(R.id.editDescriere);
        editSpecializare = findViewById(R.id.editSpecializare);
        editExperienta = findViewById(R.id.editExperienta);
        textLoginLink = findViewById(R.id.textLoginLink);
        textTitle = findViewById(R.id.titleText);
        signUpButton = findViewById(R.id.signUpButton);
        spinnerRole = findViewById(R.id.spinnerRole);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_roles,
                android.R.layout.simple_spinner_item
        );
        isEvaluatorSwitch = findViewById(R.id.switchEvaluator);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
        signUpButton.setOnClickListener(view -> {
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
            boolean evaluator = isEvaluatorSwitch.isChecked(); // assume this is a Switch

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                User newUser = new User(
                                        address,
                                        descriere,
                                        email,
                                        evaluator,
                                        experienta,
                                        firstName,
                                        lastName,
                                        phone,
                                        spinnerRole.getSelectedItem().toString(),
                                        specializare,
                                        uid
                                );

                                saveUserToFirestore(newUser);
                            }
                        } else {
                            Toast.makeText(this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        textLoginLink.setOnClickListener(view -> {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        });

    }
    private void saveUserToFirestore(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
