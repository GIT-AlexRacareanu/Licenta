package com.example.abatherapy.Activities.Therapist;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abatherapy.Models.Children;
import com.example.abatherapy.Models.Appointment;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TherapistAppointmentsFragment extends Fragment {

    private User user;
    private RecyclerView recyclerToday, recyclerOld, recyclerFuture;
    private AppointmentAdapter todayAdapter, oldAdapter, futureAdapter;
    private List<AppointmentItem> todayAppointments = new ArrayList<>();
    private List<AppointmentItem> oldAppointments = new ArrayList<>();
    private List<AppointmentItem> futureAppointments = new ArrayList<>();
    private Button btnAddAppointment;

    public static TherapistAppointmentsFragment newInstance(User user) {
        TherapistAppointmentsFragment fragment = new TherapistAppointmentsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.therapist_appointments_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerViews
        recyclerToday = view.findViewById(R.id.recyclerTodayAppointments);
        recyclerToday.setLayoutManager(new LinearLayoutManager(getContext()));
        todayAdapter = new AppointmentAdapter(todayAppointments);
        recyclerToday.setAdapter(todayAdapter);

        recyclerOld = view.findViewById(R.id.recyclerOldAppointments);
        recyclerOld.setLayoutManager(new LinearLayoutManager(getContext()));
        oldAdapter = new AppointmentAdapter(oldAppointments);
        recyclerOld.setAdapter(oldAdapter);

        recyclerFuture = view.findViewById(R.id.recyclerFutureAppointments);
        recyclerFuture.setLayoutManager(new LinearLayoutManager(getContext()));
        futureAdapter = new AppointmentAdapter(futureAppointments);
        recyclerFuture.setAdapter(futureAdapter);

        btnAddAppointment = view.findViewById(R.id.btnAddAppointment);
        btnAddAppointment.setOnClickListener(v -> showCreateAppointmentDialog());

        loadAppointments();
    }

    private void loadAppointments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        todayAppointments.clear();
        oldAppointments.clear();
        futureAppointments.clear();

        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new java.util.Date());

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null) {
                            String childId = appt.getChildId();
                            db.collection("children").document(childId).get()
                                    .addOnSuccessListener(childDoc -> {
                                        String childName = childDoc.exists() ? childDoc.getString("name") : "Unknown";

                                        AppointmentItem item = new AppointmentItem(
                                                childName,
                                                appt.getParentId(),       // <- make sure your Appointment model has getParentId()
                                                appt.getTherapistId(),
                                                appt.getDate(),
                                                appt.getStatus(),
                                                appt.getAppointmentId(),
                                                appt.getDescription()
                                        );

                                        String datePart = appt.getDate().split(" ")[0];

                                        if (datePart.equals(todayStr)) {
                                            todayAppointments.add(item);
                                            todayAdapter.notifyDataSetChanged();
                                        } else if (datePart.compareTo(todayStr) < 0) {
                                            oldAppointments.add(item);
                                            oldAdapter.notifyDataSetChanged();
                                        } else { // future
                                            futureAppointments.add(item);
                                            futureAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }

    private void showCreateAppointmentDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.create_appointment_dialog, null);

        TextView txtListName = dialogView.findViewById(R.id.txtListName);
        Spinner spinnerChild = dialogView.findViewById(R.id.spinnerTherapist);
        Spinner spinnerTimeSlot = dialogView.findViewById(R.id.spinnerTimeSlot);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        EditText edtNotes = dialogView.findViewById(R.id.edtNotes);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        spinnerTimeSlot.setEnabled(false);
        final String[] selectedDate = {null};
        List<Children> childList = new ArrayList<>();
        List<String> childNames = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        txtListName.setText("Select Children");

        db.collection("children").get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query) {
               Children child = doc.toObject(Children.class);
                if (child != null) {
                    childList.add(child);
                    childNames.add(child.getFirstName() + " " + child.getLastName());
                }
            }
            ArrayAdapter<String> adapterChild = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, childNames);
            adapterChild.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChild.setAdapter(adapterChild);
        });

        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        spinnerTimeSlot.setEnabled(true);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        btnCreate.setOnClickListener(v -> {
            int selectedChildIndex = spinnerChild.getSelectedItemPosition();
            if (selectedChildIndex < 0 || selectedChildIndex >= childList.size()) {
                Toast.makeText(getContext(), "Select a child", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDate[0] == null) {
                Toast.makeText(getContext(), "Pick a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!spinnerTimeSlot.isEnabled() || spinnerTimeSlot.getSelectedItem() == null) {
                Toast.makeText(getContext(), "Select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }

            String timeSlot = spinnerTimeSlot.getSelectedItem().toString();
            Children selectedChild = childList.get(selectedChildIndex);
            String notes = edtNotes.getText().toString();

            Appointment newAppt = new Appointment();
            newAppt.setChildId(selectedChild.getChildId());
            newAppt.setTherapistId(user.getUid());
            newAppt.setDate(selectedDate[0] + " " + timeSlot);
            newAppt.setStatus("pending");
            newAppt.setDescription(notes);

            String newDocId = db.collection("appointments").document().getId();
            newAppt.setAppointmentId(newDocId);

            db.collection("appointments").document(newDocId).set(newAppt)
                    .addOnSuccessListener(aVoid -> {
                        AppointmentItem item = new AppointmentItem(
                                selectedChild.getFirstName() + " " + selectedChild.getLastName(),
                                selectedChild.getParentId(),  // <- assuming your User class has getParentId() for child
                                user.getUid(),
                                selectedDate[0] + " " + timeSlot,
                                "pending",
                                newDocId,
                                notes
                        );

                        // Add to correct list
                        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(new java.util.Date());
                        String datePart = selectedDate[0];

                        if (datePart.equals(todayStr)) {
                            todayAppointments.add(item);
                            todayAdapter.notifyDataSetChanged();
                        } else if (datePart.compareTo(todayStr) < 0) {
                            oldAppointments.add(item);
                            oldAdapter.notifyDataSetChanged();
                        } else {
                            futureAppointments.add(item);
                            futureAdapter.notifyDataSetChanged();
                        }

                        Toast.makeText(getContext(), "Appointment created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private static class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private final List<AppointmentItem> items;

        AppointmentAdapter(List<AppointmentItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.therapist_item_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentItem item = items.get(position);

            holder.txtChildName.setText(item.childName);
            holder.txtDate.setText(item.date);
            holder.txtNotes.setText(item.notes != null ? item.notes : "");

            // Set status text
            holder.txtStatus.setText(item.status);

            // Set status indicator color
            int color;
            switch (item.status.toLowerCase(Locale.ROOT)) {
                case "accepted":
                    color = 0xFF4CAF50; // green
                    break;
                case "pending":
                    color = 0xFFFFC107; // amber
                    break;
                case "rejected":
                    color = 0xFFF44336; // red
                    break;
                default:
                    color = 0xFF9E9E9E; // grey
            }
            holder.viewStatusIndicator.setBackgroundColor(color);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtChildName, txtDate, txtStatus, txtNotes;
            View viewStatusIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                txtChildName = itemView.findViewById(R.id.txtChildName);
                txtDate = itemView.findViewById(R.id.txtDate);
                txtStatus = itemView.findViewById(R.id.txtStatus);
                txtNotes = itemView.findViewById(R.id.txtNotes);
                viewStatusIndicator = itemView.findViewById(R.id.viewStatusIndicator);
            }
        }
    }
    private static class AppointmentItem {
        String childName, parentId, therapistId, date, status, appointmentId, notes;

        AppointmentItem(String childName, String parentId, String therapistId, String date, String status, String appointmentId, String notes) {
            this.childName = childName;
            this.parentId = parentId;
            this.therapistId = therapistId;
            this.date = date;
            this.status = status;
            this.appointmentId = appointmentId;
            this.notes = notes;
        }
    }

}
