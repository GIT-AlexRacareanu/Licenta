package com.example.abatherapy.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abatherapy.Activities.Evaluator.EvaluatorActivity;
import com.example.abatherapy.Activities.Patient.PatientActivity;
import com.example.abatherapy.Activities.Therapist.TherapistActivity;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {
    Button logInButton;
    EditText editEmail,editPassword;
    TextView textSignUpLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_activity);
        logInButton = findViewById(R.id.buttonLogin);
        editEmail = findViewById(R.id.editTextEmail);
        editPassword = findViewById(R.id.editTextPassword);
        textSignUpLink = findViewById(R.id.textSignUpLink);
        EdgeToEdge.enable(this);
        logInButton.setOnClickListener(view -> {
            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                User.databaseMapper(user, actualUser -> {
                                    if (actualUser != null) {
                                        Intent intent = null;
                                        switch(actualUser.getRole())
                                        {
                                            case "Pacient":
                                                intent = new Intent(this, PatientActivity.class);
                                                intent.putExtra("user", actualUser);
                                                startActivity(intent);
                                                finish();
                                                break;
                                            case "Terapeut":
                                                if(actualUser.isEvaluator()) {
                                                    intent = new Intent(this, EvaluatorActivity.class);
                                                    intent.putExtra("user", actualUser);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    intent = new Intent(this, TherapistActivity.class);
                                                    intent.putExtra("user", actualUser);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                    } else {
                                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        textSignUpLink.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

    }
}