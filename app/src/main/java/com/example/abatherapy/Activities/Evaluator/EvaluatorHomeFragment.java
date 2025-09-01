package com.example.abatherapy.Activities.Evaluator;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvaluatorHomeFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;

    private TextView txtGreeting;
    private RecyclerView recyclerToday, recyclerOld;
    private TodayAppointmentsAdapter adapter, oldAdapter;
    private List<Appointment> todayAppointments = new ArrayList<>();
    private List<Appointment> oldAppointments = new ArrayList<>();
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
        adapter = new TodayAppointmentsAdapter(todayAppointments, null);
        recyclerToday.setAdapter(adapter);

        recyclerOld = view.findViewById(R.id.recyclerOldAppointments);
        recyclerOld.setLayoutManager(new LinearLayoutManager(getContext()));
        oldAdapter = new TodayAppointmentsAdapter(oldAppointments, this::createNewFromOld);
        recyclerOld.setAdapter(oldAdapter);

        if (user != null) {
            txtGreeting.setText("Hello, " + user.getFirstName() + " " + user.getLastName() + "!");
            loadAppointments();
        }
    }

    // Load today's appointments
    private void loadTodayAppointments() {
        if (user == null) return;
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    todayAppointments.clear();
                    for (DocumentSnapshot doc : query) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null && appt.getDate().startsWith(todayStr)) {
                            todayAppointments.add(appt);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load today's appointments", Toast.LENGTH_SHORT).show());
    }

    // Load all appointments
    private void loadAppointments() {
        if (user == null) return;
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    todayAppointments.clear();
                    oldAppointments.clear();
                    for (DocumentSnapshot doc : query) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null) {
                            if (appt.getDate().startsWith(todayStr)) {
                                todayAppointments.add(appt);
                            } else if (appt.getDate().compareTo(todayStr) < 0) {
                                oldAppointments.add(appt);
                            }
                        }
                    }
                    todayAppointments.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                    oldAppointments.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                    adapter.notifyDataSetChanged();
                    oldAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to load appointments", Toast.LENGTH_SHORT).show());
    }

    // Create new appointment from old
    private void createNewFromOld(Appointment oldAppt) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.create_appointment_dialog, null);

        final Spinner spinnerTherapist = dialogView.findViewById(R.id.spinnerTherapist);
        final Spinner spinnerTimeSlot = dialogView.findViewById(R.id.spinnerTimeSlot);
        final Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        final EditText edtNotes = dialogView.findViewById(R.id.edtNotes);
        TextView listName = dialogView.findViewById(R.id.txtListName);
        final Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        spinnerTimeSlot.setEnabled(false);
        final String[] selectedDate = {null};

        listName.setText("Select Therapist");
        final List<User> therapistList = new ArrayList<>();
        final List<String> therapistNames = new ArrayList<>();
        db.collection("users")
                .whereEqualTo("role", "Terapeut")
                .whereEqualTo("evaluator", false)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        User therapist = doc.toObject(User.class);
                        if (therapist != null) {
                            therapistList.add(therapist);
                            therapistNames.add(therapist.getFirstName() + " " + therapist.getLastName());
                        }
                    }
                    ArrayAdapter<String> adapterTherapist = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, therapistNames);
                    adapterTherapist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTherapist.setAdapter(adapterTherapist);
                });

        // Pick date and load available time slots
        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate[0] = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                        int selectedTherapistIndex = spinnerTherapist.getSelectedItemPosition();
                        if (selectedTherapistIndex < 0 || selectedTherapistIndex >= therapistList.size()) {
                            Toast.makeText(getContext(), "Please select a therapist first", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        User selectedTherapist = therapistList.get(selectedTherapistIndex);

                        // Check existing appointments
                        db.collection("appointments")
                                .whereEqualTo("therapistId", selectedTherapist.getUid())
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<String> occupiedSlots = new ArrayList<>();
                                    for (DocumentSnapshot doc : querySnapshot) {
                                        Appointment appt = doc.toObject(Appointment.class);
                                        if (appt != null && appt.getDate().startsWith(selectedDate[0])) {
                                            String[] parts = appt.getDate().split(" ");
                                            if (parts.length == 2) occupiedSlots.add(parts[1]);
                                        }
                                    }

                                    String[] allSlots = {"08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00"};
                                    List<String> availableSlots = new ArrayList<>();
                                    for (String slot : allSlots) {
                                        if (!occupiedSlots.contains(slot)) availableSlots.add(slot);
                                    }

                                    if (availableSlots.isEmpty()) {
                                        Toast.makeText(getContext(), "No available time slots on this date", Toast.LENGTH_SHORT).show();
                                        spinnerTimeSlot.setEnabled(false);
                                    } else {
                                        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(),
                                                android.R.layout.simple_spinner_item, availableSlots);
                                        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerTimeSlot.setAdapter(timeAdapter);
                                        spinnerTimeSlot.setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to check time slots", Toast.LENGTH_SHORT).show());

                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        // Create appointment
        btnCreate.setOnClickListener(v -> {
            if (selectedDate[0] == null) {
                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTherapistIndex = spinnerTherapist.getSelectedItemPosition();
            if (selectedTherapistIndex < 0 || selectedTherapistIndex >= therapistList.size()) {
                Toast.makeText(getContext(), "Please select a therapist", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!spinnerTimeSlot.isEnabled() || spinnerTimeSlot.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedTimeSlot = spinnerTimeSlot.getSelectedItem().toString();
            User selectedTherapist = therapistList.get(selectedTherapistIndex);
            String notes = edtNotes.getText().toString();

            Appointment newAppt = new Appointment();
            newAppt.setChildId(oldAppt.getChildId());
            newAppt.setParentId(oldAppt.getParentId());
            newAppt.setTherapistId(selectedTherapist.getUid());
            newAppt.setDate(selectedDate[0]);
            newAppt.setTime(selectedTimeSlot);
            newAppt.setStatus("pending");
            newAppt.setDescription(notes);

            String newDocId = db.collection("appointments").document().getId();
            newAppt.setAppointmentId(newDocId);

            db.collection("appointments").document(newDocId).set(newAppt)
                    .addOnSuccessListener(aVoid -> {
                        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        if (selectedDate[0].equals(todayStr)) {
                            todayAppointments.add(newAppt);
                            adapter.notifyDataSetChanged();
                        } else {
                            oldAppointments.add(newAppt);
                            oldAdapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getContext(), "New appointment created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create appointment", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private static class TodayAppointmentsAdapter extends RecyclerView.Adapter<AppointmentViewHolder> {
        private final List<Appointment> appointments;
        private final OnOldAppointmentClickListener oldClickListener;

        TodayAppointmentsAdapter(List<Appointment> appointments, OnOldAppointmentClickListener listener) {
            this.appointments = appointments;
            this.oldClickListener = listener;
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

            if (oldClickListener != null) {
                holder.itemView.setOnClickListener(v -> oldClickListener.onOldAppointmentClick(appt));
            }
        }

        @Override
        public int getItemCount() { return appointments.size(); }

        public interface OnOldAppointmentClickListener {
            void onOldAppointmentClick(Appointment appointment);
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
