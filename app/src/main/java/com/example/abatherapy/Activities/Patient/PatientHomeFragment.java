package com.example.abatherapy.Activities.Patient;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abatherapy.Models.Appointment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientHomeFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;

    private TextView txtGreeting, txtSubtitle;
    private RecyclerView recyclerAppointments;
    private List<Appointment> upcomingAppointments = new ArrayList<>();
    private AppointmentsAdapter appointmentsAdapter;

    private FirebaseFirestore db;

    public static PatientHomeFragment newInstance(User user) {
        PatientHomeFragment fragment = new PatientHomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtGreeting = view.findViewById(R.id.txtGreeting);
        txtSubtitle = view.findViewById(R.id.txtSubtitle);
        recyclerAppointments = view.findViewById(R.id.recyclerUpcomingAppointments);

        recyclerAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        appointmentsAdapter = new AppointmentsAdapter(upcomingAppointments);
        recyclerAppointments.setAdapter(appointmentsAdapter);

        if (user != null) {
            txtGreeting.setText("Bine te-am găsit, " + user.getFirstName() + " " + user.getLastName() + "!");
            txtSubtitle.setText("Future Appointments");
            loadUpcomingAppointments();
        }
    }

    private void loadUpcomingAppointments() {
        if (user == null) return;

        db.collection("appointments")
                .whereEqualTo("parentId", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    upcomingAppointments.clear();
                    String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    for (DocumentSnapshot doc : querySnapshot) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null && appt.getDate().compareTo(todayStr) >= 0) {
                            upcomingAppointments.add(appt);
                        }
                    }

                    upcomingAppointments.sort((a1, a2) -> a1.getDate().compareTo(a2.getDate()));
                    appointmentsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Eroare la încărcarea programărilor", Toast.LENGTH_SHORT).show());
    }

    private static class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsViewHolder> {

        private final List<Appointment> appointments;

        AppointmentsAdapter(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        @Override
        public AppointmentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_appointment, parent, false);
            return new AppointmentsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppointmentsViewHolder holder, int position) {
            Appointment appt = appointments.get(position);
            holder.bind(
                    appt.getDate(),
                    appt.getTherapistId(),
                    appt.getChildId()
            );
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }
    }

    private static class AppointmentsViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtDate, txtTherapist, txtChild;

        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        public AppointmentsViewHolder(View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTherapist = itemView.findViewById(R.id.txtTherapist);
            txtChild = itemView.findViewById(R.id.txtChild);
        }

        public void bind(String date, String therapistId, String childId) {
            txtDate.setText(date);

            if (therapistId != null && !therapistId.isEmpty()) {
                db.collection("users").document(therapistId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String name = doc.getString("firstName") + " " + doc.getString("lastName");
                                txtTherapist.setText("Therapist: " + (name != null ? name : "Unknown"));
                            } else {
                                txtTherapist.setText("Therapist: Unknown");
                            }
                        })
                        .addOnFailureListener(e -> txtTherapist.setText("Therapist: Error"));
            }

            if (childId != null && !childId.isEmpty()) {
                db.collection("children").document(childId).get()
                        .addOnSuccessListener(doc -> {
                            Log.d("TEEEEEST", doc.getId());
                            if (doc.exists()) {
                                String name = doc.getString("firstName") + " " + doc.getString("lastName");
                                txtChild.setText("Child: " + (name != null ? name : "Unknown"));
                            } else {
                                txtChild.setText("Child: Unknown");
                            }
                        })
                        .addOnFailureListener(e -> txtChild.setText("Child: Error"));
            }
        }
    }
}
