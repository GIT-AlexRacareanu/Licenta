package com.example.abatherapy.Activities.Evaluator;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abatherapy.Models.Appointment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EvaluatorAppointmentsFragment extends Fragment {

    private User user;
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private List<Appointment> appointments = new ArrayList<>();
    private FirebaseFirestore db;

    public static EvaluatorAppointmentsFragment newInstance(User user) {
        EvaluatorAppointmentsFragment fragment = new EvaluatorAppointmentsFragment();
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
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.evaluator_appointments_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentsAdapter(appointments);
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadAppointmentsForDate(selectedDate);
        });

        Calendar cal = Calendar.getInstance();
        String today = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        loadAppointmentsForDate(today);
    }

    private void loadAppointmentsForDate(String date) {
        if (user == null) return;
        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointments.clear();
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                Appointment appt = doc.toObject(Appointment.class);
                                if (appt != null) {
                                    appt.setAppointmentId(doc.getId());  // <-- IMPORTANT
                                    appointments.add(appt);
                                }
                            }
                    adapter.notifyDataSetChanged();
                });
    }

    private class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsViewHolder> {
        private final List<Appointment> appointments;
        AppointmentsAdapter(List<Appointment> appointments) { this.appointments = appointments; }

        @Override
        public AppointmentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_appointment, parent, false);
            return new AppointmentsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppointmentsViewHolder holder, int position) {
            Appointment appt = appointments.get(position);
            holder.txtDate.setText(appt.getDate());

            FirebaseFirestore.getInstance().collection("users")
                    .document(appt.getTherapistId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String therapistName = doc.getString("firstName") + " " + doc.getString("lastName");
                            holder.txtTherapist.setText("Therapist: " + therapistName);
                        }
                    });

            FirebaseFirestore.getInstance().collection("children")
                    .document(appt.getChildId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String childName = doc.getString("firstName") + " " + doc.getString("lastName");
                            holder.txtChild.setText("Child: " + childName);
                        }
                    });

            holder.itemView.setOnClickListener(v -> {
                AppointmentDetailsDialog appointmentDetailsDialog = new AppointmentDetailsDialog();
                AppointmentDetailsDialog dialog = appointmentDetailsDialog.newInstance(appt);
                FragmentManager fm = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                dialog.show(fm, "AppointmentDetailsDialog");
            });
        }

        @Override
        public int getItemCount() { return appointments.size(); }
    }

    private static class AppointmentsViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTherapist, txtChild;
        public AppointmentsViewHolder(View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTherapist = itemView.findViewById(R.id.txtTherapist);
            txtChild = itemView.findViewById(R.id.txtChild);
        }
    }

    public static class AppointmentDetailsDialog extends DialogFragment {

        private static final String ARG_APPT = "appointment";
        private Appointment appointment;

        public static AppointmentDetailsDialog newInstance(Appointment appt) {
            AppointmentDetailsDialog dialog = new AppointmentDetailsDialog();
            Bundle args = new Bundle();
            args.putSerializable(ARG_APPT, appt);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            if (getArguments() != null) {
                appointment = (Appointment) getArguments().getSerializable(ARG_APPT);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Appointment Details");

            final View view = getLayoutInflater().inflate(R.layout.evaluator_appointment_details, null);

            TextView txtDate = view.findViewById(R.id.txtDate);
            TextView txtChild = view.findViewById(R.id.txtChild);
            TextView txtParent = view.findViewById(R.id.txtParent);
            TextView txtStatus = view.findViewById(R.id.txtStatus);
            Button btnAction = view.findViewById(R.id.btnActions);
            TextView edtNotes = view.findViewById(R.id.edtNotes); // Add an EditText in layout for notes

            txtDate.setText("Date: " + appointment.getDate());
            txtChild.setText("Child ID: " + appointment.getChildId());
            txtParent.setText("Parent ID: " + appointment.getParentId());
            txtStatus.setText("Status: " + appointment.getStatus());

            // Compare appointment date with today
            Calendar cal = Calendar.getInstance();
            String today = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

            int comparison = appointment.getDate().compareTo(today);

            if (comparison < 0) {
                // Past appointment
                btnAction.setText("Send to Therapist");
                edtNotes.setVisibility(View.GONE);

                btnAction.setOnClickListener(v -> {
                    // Show a dialog with the list of therapists
                    FirebaseFirestore.getInstance().collection("users")
                            .whereEqualTo("role", "therapist")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<User> therapists = new ArrayList<>();
                                for (DocumentSnapshot doc : querySnapshot) {
                                    User therapist = doc.toObject(User.class);
                                    if (therapist != null) therapists.add(therapist);
                                }

                                showTherapistSelectionDialog(appointment);
                            });
                });
            } else if (comparison == 0) {
                btnAction.setText("Save Notes");
                edtNotes.setVisibility(View.VISIBLE);
                edtNotes.setText(appointment.getDescription() != null ? appointment.getDescription() : "");
                btnAction.setOnClickListener(v -> {
                    String notes = edtNotes.getText().toString().trim();
                    FirebaseFirestore.getInstance().collection("appointments")
                            .document(appointment.getAppointmentId())
                            .update("description", notes)
                            .addOnSuccessListener(aVoid -> {
                                txtStatus.setText("Notes saved");
                            });
                });

            } else {
                btnAction.setText("Accept Appointment");
                edtNotes.setVisibility(View.GONE);
                btnAction.setOnClickListener(v -> {
                    FirebaseFirestore.getInstance().collection("appointments")
                            .document(appointment.getAppointmentId())
                            .update("status", "accepted")
                            .addOnSuccessListener(aVoid -> {
                                txtStatus.setText("Status: accepted");
                            });
                });
            }

            builder.setView(view);
            builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
            return builder.create();
        }

        private void showTherapistSelectionDialog(Appointment appointment) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .whereEqualTo("role", "Terapeut")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<User> therapists = new ArrayList<>();
                        List<String> therapistNames = new ArrayList<>();

                        for (DocumentSnapshot doc : querySnapshot) {
                            User therapist = doc.toObject(User.class);
                            if (therapist != null) {
                                therapists.add(therapist);
                                therapistNames.add(therapist.getFirstName() + " " + therapist.getLastName());
                            }
                        }

                        if (therapists.isEmpty()) {
                            Toast.makeText(getContext(), "No therapists available", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] namesArray = therapistNames.toArray(new String[0]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Select a Therapist");
                        builder.setItems(namesArray, (dialog, which) -> {
                            User selectedTherapist = therapists.get(which);

                            db.collection("appointments")
                                    .document(appointment.getAppointmentId())
                                    .update("therapistId", selectedTherapist.getUid(), "status", "sent")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(),
                                                "Appointment sent to " + namesArray[which],
                                                Toast.LENGTH_SHORT).show();
                                    });
                        });

                        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                        builder.show();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to fetch therapists", Toast.LENGTH_SHORT).show();
                    });
        }

    }


}
