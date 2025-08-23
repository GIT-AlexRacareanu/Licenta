package com.example.abatherapy.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abatherapy.Appointment;
import com.example.abatherapy.Calendar.BlockedDatesValidator;
import com.example.abatherapy.Children;
import com.example.abatherapy.Database.DBManager;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {

    TextView helloText;
    ListView kidsListView;
    Button addChildButton, listEvaluator, listTherapists;
    ArrayList<Children> childrenList;
    ArrayAdapter<Children> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity);

        helloText = findViewById(R.id.helloText);
        kidsListView = findViewById(R.id.kidListView);
        addChildButton = findViewById(R.id.addChildButton);
        listEvaluator = findViewById(R.id.listEvaluator);
        listTherapists = findViewById(R.id.listTherapists);

        User user = (User) getIntent().getSerializableExtra("user");

        childrenList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, childrenList);
        kidsListView.setAdapter(adapter);

        loadKids(user.getUid());

        helloText.setText("Bine te-am gasit, " + user.getFirstName() + " " + user.getLastName() + "!");

        kidsListView.setOnItemClickListener((parent, view, position, id) -> {
            Children selectedChild = (Children) parent.getItemAtPosition(position);
            new AlertDialog.Builder(PatientActivity.this)
                    .setTitle("Detalii copil")
                    .setMessage("Nume: " + selectedChild.getFirstName() + " " + selectedChild.getLastName()
                            + "\nVârstă: " + selectedChild.getAge()
                            + "\nData nașterii: " + selectedChild.getBirthDate()
                            + "\nCNP: " + selectedChild.getCnp())
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Șterge", (dialog, which) -> deleteChild(selectedChild))
                    .show();
        });

        addChildButton.setOnClickListener(v -> showAddChildDialog(user.getUid()));

        listEvaluator.setOnClickListener(v -> showListEvaluators(user.getUid()));
    }

    private void loadKids(String parentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        childrenList.clear();
        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Children child = doc.toObject(Children.class);
                        if (child != null) childrenList.add(child);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddChildDialog(String parentId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_child_dialog, null);

        EditText firstName = dialogView.findViewById(R.id.editFirstName);
        EditText lastName = dialogView.findViewById(R.id.editLastName);
        EditText age = dialogView.findViewById(R.id.editAge);
        EditText birthDate = dialogView.findViewById(R.id.editBirthDate);
        EditText cnp = dialogView.findViewById(R.id.editCnp);

        new AlertDialog.Builder(this)
                .setTitle("Adaugă copil")
                .setView(dialogView)
                .setPositiveButton("Salvează", (dialog, which) -> {
                    String childId = java.util.UUID.randomUUID().toString();

                    Children newChild = new Children(
                            age.getText().toString(),
                            birthDate.getText().toString(),
                            firstName.getText().toString(),
                            lastName.getText().toString(),
                            cnp.getText().toString(),
                            childId,
                            parentId
                    );

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("children")
                            .document(childId)
                            .set(newChild)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Copil adăugat", Toast.LENGTH_SHORT).show();
                                loadKids(parentId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Eroare la salvare", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void showListEvaluators(String parentId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.evaluator_dialog, null);
        ListView evaluatorListView = dialogView.findViewById(R.id.evaluatorListView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<User> evaluatorList = new ArrayList<>();
        ArrayAdapter<User> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, evaluatorList);

        db.collection("users")
                .whereEqualTo("evaluator", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User evaluator = doc.toObject(User.class);
                        if (evaluator != null) evaluatorList.add(evaluator);
                    }
                    evaluatorListView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Alege un evaluator")
                            .setView(dialogView)
                            .setNegativeButton("Închide", null)
                            .create();
                    dialog.show();

                    evaluatorListView.setOnItemClickListener((parent, view, position, id) -> {
                        User selectedEvaluator = evaluatorList.get(position);

                        DBManager.getAppointments(selectedEvaluator, unavailableDates -> {
                            List<Long> blockedTimestamps = new ArrayList<>();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            for (String d : unavailableDates) {
                                try {
                                    blockedTimestamps.add(sdf.parse(d).getTime());
                                } catch (ParseException e) { e.printStackTrace(); }
                            }

                            BlockedDatesValidator validator = new BlockedDatesValidator(unavailableDates);

                            CalendarConstraints constraints = new CalendarConstraints.Builder()
                                    .setValidator(validator)
                                    .build();

                            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                                    .setTitleText("Alege o dată disponibilă")
                                    .setCalendarConstraints(constraints)
                                    .build();

                            picker.show(getSupportFragmentManager(), "DATE_PICKER");

                            picker.addOnPositiveButtonClickListener(selection -> {
                                String chosenDate = sdf.format(new Date(selection));

                                List<String> childNames = new ArrayList<>();
                                for (Children c : childrenList) {
                                    childNames.add(c.getFirstName() + " " + c.getLastName());
                                }

                                CharSequence[] childArray = childNames.toArray(new CharSequence[0]);
                                new AlertDialog.Builder(this)
                                        .setTitle("Alege copilul pentru programare")
                                        .setItems(childArray, (childrenDialog, which) -> {
                                            Children selectedChild = childrenList.get(which);

                                            String appointmentId = java.util.UUID.randomUUID().toString();
                                            Appointment appointment = new Appointment(
                                                    chosenDate,"",parentId,"",selectedEvaluator.getUid()
                                            );

                                            FirebaseFirestore.getInstance()
                                                    .collection("appointments")
                                                    .document(appointmentId)
                                                    .set(appointment)
                                                    .addOnSuccessListener(aVoid ->
                                                            Toast.makeText(this, "Programarea a fost salvată: " + chosenDate, Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this, "Eroare la salvarea programării", Toast.LENGTH_SHORT).show());
                                        })
                                        .show();
                            });
                        });
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Eroare la încărcarea evaluatorilor", Toast.LENGTH_SHORT).show());
    }

    private void deleteChild(Children child) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("children")
                .whereEqualTo("childId", child.getChildId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            doc.getReference().delete();
                        }
                        Toast.makeText(this, "Copil șters", Toast.LENGTH_SHORT).show();
                        loadKids(child.getParentId());
                    }
                });
    }
}
