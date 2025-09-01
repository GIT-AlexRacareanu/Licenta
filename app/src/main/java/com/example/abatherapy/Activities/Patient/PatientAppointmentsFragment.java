package com.example.abatherapy.Activities.Patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abatherapy.Calendar.BlockedDatesValidator;
import com.example.abatherapy.Models.Children;
import com.example.abatherapy.Models.Appointment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatientAppointmentsFragment extends Fragment {

    private static final String ARG_USER = "user";
    private ArrayList<Children> childrenList = new ArrayList<>();
    private User user;
    private FirebaseFirestore db;
    private RecyclerView recyclerToday, recyclerUpcoming, recyclerEnded;
    private AppointmentsAdapter adapterToday, adapterUpcoming, adapterEnded;
    private List<Appointment> todayAppointments = new ArrayList<>();
    private List<Appointment> upcomingAppointments = new ArrayList<>();
    private List<Appointment> endedAppointments = new ArrayList<>();

    private final String[] TIME_SLOTS = {
            "08:00-10:00",
            "10:00-12:00",
            "12:00-14:00",
            "14:00-16:00"
    };

    public PatientAppointmentsFragment() {}

    public static PatientAppointmentsFragment newInstance(User user) {
        PatientAppointmentsFragment fragment = new PatientAppointmentsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) user = (User) getArguments().getSerializable(ARG_USER);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_appointments_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerToday = view.findViewById(R.id.recyclerToday);
        recyclerUpcoming = view.findViewById(R.id.recyclerUpcoming);
        recyclerEnded = view.findViewById(R.id.recyclerEnded);

        recyclerToday.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerEnded.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterToday = new AppointmentsAdapter(todayAppointments);
        adapterUpcoming = new AppointmentsAdapter(upcomingAppointments);
        adapterEnded = new AppointmentsAdapter(endedAppointments);

        recyclerToday.setAdapter(adapterToday);
        recyclerUpcoming.setAdapter(adapterUpcoming);
        recyclerEnded.setAdapter(adapterEnded);

        FloatingActionButton fab = view.findViewById(R.id.fabAddAppointment);
        fab.setOnClickListener(v -> {
            if (user != null) showAddAppointmentDialog(user.getUid());
        });

        loadChildren(user.getUid());
        loadAppointments();
    }

    private void loadAppointments() {
        if (user == null) return;

        db.collection("appointments")
                .whereEqualTo("parentId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todayAppointments.clear();
                    upcomingAppointments.clear();
                    endedAppointments.clear();

                    Date now = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String todayStr = sdf.format(now);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null) {
                            String apptDate = appt.getDate();
                            if (apptDate.equals(todayStr)) todayAppointments.add(appt);
                            else if (apptDate.compareTo(todayStr) > 0) upcomingAppointments.add(appt);
                            else endedAppointments.add(appt);
                        }
                    }

                    todayAppointments.sort(Comparator.comparing(Appointment::getDate));
                    upcomingAppointments.sort(Comparator.comparing(Appointment::getDate));
                    endedAppointments.sort(Comparator.comparing(Appointment::getDate));

                    adapterToday.notifyDataSetChanged();
                    adapterUpcoming.notifyDataSetChanged();
                    adapterEnded.notifyDataSetChanged();
                });
    }

    private void loadChildren(String parentId) {
        childrenList.clear();
        db.collection("children")
                .whereEqualTo("parentId", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Children child = doc.toObject(Children.class);
                        if (child != null) childrenList.add(child);
                    }
                });
    }

    private void showAddAppointmentDialog(String parentId) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.evaluator_dialog, null);
        ListView evaluatorListView = dialogView.findViewById(R.id.evaluatorListView);

        ArrayList<User> evaluatorList = new ArrayList<>();
        ArrayAdapter<User> evalAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, evaluatorList);

        db.collection("users")
                .whereEqualTo("evaluator", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User evaluator = doc.toObject(User.class);
                        if (evaluator != null) evaluatorList.add(evaluator);
                    }
                    evaluatorListView.setAdapter(evalAdapter);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Alege un evaluator")
                            .setView(dialogView)
                            .setNegativeButton("Închide", null)
                            .show();

                    evaluatorListView.setOnItemClickListener((parent, view, position, id) -> {
                        User selectedEvaluator = evaluatorList.get(position);
                        pickDateForEvaluator(selectedEvaluator, parentId);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Eroare la încărcarea evaluatorilor", Toast.LENGTH_SHORT).show());
    }

    private void pickDateForEvaluator(User evaluator, String parentId) {
        db.collection("appointments")
                .whereEqualTo("therapistId", evaluator.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Set<String>> appointmentsPerDate = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null) {
                            appointmentsPerDate.computeIfAbsent(appt.getDate(), k -> new HashSet<>());
                            if (appt.getTime() != null) appointmentsPerDate.get(appt.getDate()).add(appt.getTime());
                        }
                    }

                    Set<Long> fullyBookedDaysMillis = new HashSet<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    for (Map.Entry<String, Set<String>> entry : appointmentsPerDate.entrySet()) {
                        if (entry.getValue().size() >= TIME_SLOTS.length) {
                            try { fullyBookedDaysMillis.add(sdf.parse(entry.getKey()).getTime()); }
                            catch (Exception e) { e.printStackTrace(); }
                        }
                    }

                    CalendarConstraints constraints = new CalendarConstraints.Builder()
                            .setValidator(new BlockedDatesValidator(fullyBookedDaysMillis))
                            .build();

                    MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Alege o dată disponibilă")
                            .setCalendarConstraints(constraints)
                            .build();

                    picker.show(getParentFragmentManager(), "DATE_PICKER");

                    picker.addOnPositiveButtonClickListener(selection -> {
                        String chosenDate = sdf.format(new Date(selection));
                        Set<String> unavailableSlots = appointmentsPerDate.getOrDefault(chosenDate, new HashSet<>());
                        List<String> availableSlots = new ArrayList<>();
                        for (String slot : TIME_SLOTS)
                            if (!unavailableSlots.contains(slot)) availableSlots.add(slot);

                        if (availableSlots.isEmpty()) {
                            Toast.makeText(requireContext(), "Nu sunt intervale disponibile.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        CharSequence[] slotsArray = availableSlots.toArray(new CharSequence[0]);
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Alege interval orar")
                                .setItems(slotsArray, (dialog, which) -> {
                                    String chosenTime = availableSlots.get(which);
                                    chooseChildForAppointment(parentId, evaluator.getUid(), chosenDate, chosenTime);
                                }).show();
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Eroare la încărcarea programărilor evaluatorului", Toast.LENGTH_SHORT).show());
    }

    private void chooseChildForAppointment(String parentId, String therapistId, String chosenDate, String chosenTime) {
        CharSequence[] childArray = childrenList.stream()
                .map(c -> c.getFirstName() + " " + c.getLastName())
                .toArray(CharSequence[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Alege copilul pentru programare")
                .setItems(childArray, (childrenDialog, whichChild) -> {
                    Children selectedChild = childrenList.get(whichChild);

                    String appointmentId = UUID.randomUUID().toString();
                    Appointment appointment = new Appointment(
                            appointmentId,
                            selectedChild.getChildId(),
                            chosenDate,
                            chosenTime,
                            "",
                            parentId,
                            "pending",
                            therapistId
                    );

                    db.collection("appointments")
                            .document(appointmentId)
                            .set(appointment)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireContext(),
                                        "Programarea a fost salvată: " + chosenDate + " " + chosenTime,
                                        Toast.LENGTH_SHORT).show();
                                loadAppointments();
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(),
                                    "Eroare la salvarea programării", Toast.LENGTH_SHORT).show());
                })
                .show();
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
            return new AppointmentsViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(AppointmentsViewHolder holder, int position) {
            Appointment appt = appointments.get(position);
            holder.bind(appt);
        }

        @Override
        public int getItemCount() { return appointments.size(); }
    }

    private static class AppointmentsViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTherapist, txtChild;
        FirebaseFirestore db;

        public AppointmentsViewHolder(View itemView, AppointmentsAdapter adapter) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTherapist = itemView.findViewById(R.id.txtTherapist);
            txtChild = itemView.findViewById(R.id.txtChild);
            db = FirebaseFirestore.getInstance();

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Appointment appointment = adapter.appointments.get(position);
                showAppointmentDialog(appointment, adapter, position);
            });
        }

        public void bind(Appointment appointment) {
            txtDate.setText(appointment.getDate());

            if (appointment.getTherapistId() != null && !appointment.getTherapistId().isEmpty()) {
                db.collection("users").document(appointment.getTherapistId()).get()
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

            if (appointment.getChildId() != null && !appointment.getChildId().isEmpty()) {
                db.collection("children").document(appointment.getChildId()).get()
                        .addOnSuccessListener(doc -> {
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

        private void showAppointmentDialog(Appointment appointment, AppointmentsAdapter adapter, int position) {
            String info = "Date: " + appointment.getDate() + "\n"
                    + "Time: " + appointment.getTime() + "\n"
                    + txtTherapist.getText() + "\n"
                    + txtChild.getText();

            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Appointment Details")
                    .setMessage(info)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("appointments").document(appointment.getAppointmentId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(itemView.getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                                    adapter.appointments.remove(position);
                                    adapter.notifyItemRemoved(position);
                                })
                                .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Error deleting appointment", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Close", null)
                    .show();
        }
    }

}
