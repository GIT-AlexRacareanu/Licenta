package com.example.abatherapy.Activities.Evaluator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class EvaluatorHomeFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;

    private TextView txtGreeting;
    private RecyclerView recyclerToday;
    private TodayAppointmentsAdapter adapter;
    private List<Appointment> todayAppointments = new ArrayList<>();
    private FirebaseFirestore db;

    public EvaluatorHomeFragment() {}

    public static EvaluatorHomeFragment newInstance(User user) {
        EvaluatorHomeFragment fragment = new EvaluatorHomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.evaluator_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtGreeting = view.findViewById(R.id.txtGreeting);
        recyclerToday = view.findViewById(R.id.recyclerTodayAppointments);
        recyclerToday.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TodayAppointmentsAdapter(todayAppointments);
        recyclerToday.setAdapter(adapter);

        if (user != null) {
            txtGreeting.setText("Hello, " + user.getFirstName() + " " + user.getLastName() + "!");
            loadTodayAppointments();
        }
    }

    private void loadTodayAppointments() {
        if (user == null) return;

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todayAppointments.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null && todayStr.equals(appt.getDate())) {
                            todayAppointments.add(appt);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load today's appointments",
                        Toast.LENGTH_SHORT).show());
    }

    private static class TodayAppointmentsAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
        private final List<Appointment> appointments;

        TodayAppointmentsAdapter(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_appointment, parent, false);
            return new AppointmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
            Appointment appt = appointments.get(position);
            holder.bind(appt.getDate(), appt.getChildId(), appt.getParentId());
        }

        @Override
        public int getItemCount() {
            return appointments.size();
        }
    }

    private static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtChild, txtParent;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtChild = itemView.findViewById(R.id.txtChild);
            txtParent = itemView.findViewById(R.id.txtTherapist);
        }

        public void bind(String date, String childId, String parentId) {
            txtDate.setText(date);
            txtChild.setText("Child: " + childId);
            txtParent.setText("Parent: " + parentId);
        }
    }
}
