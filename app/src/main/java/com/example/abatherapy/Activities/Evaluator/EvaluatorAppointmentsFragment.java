package com.example.abatherapy.Activities.Evaluator;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EvaluatorAppointmentsFragment extends Fragment {

    private User user;
    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private final List<Appointment> appointments = new ArrayList<>();
    private FirebaseFirestore db;
    private MaterialCalendarView calendarView;
    private final Set<CalendarDay> bookedDates = new HashSet<>();

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
        AndroidThreeTen.init(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.evaluator_appointments_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentsAdapter(appointments);
        recyclerView.setAdapter(adapter);

        calendarView = view.findViewById(R.id.calendarView);

        LocalDate today = LocalDate.now();
        calendarView.state().edit()
                .setMinimumDate(today)
                .commit();

        calendarView.addDecorator(new WeekendDecorator());

        loadBookedDates();
    }

    private void loadBookedDates() {
        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    bookedDates.clear();
                    for (DocumentSnapshot doc : query) {
                        String dateStr = doc.getString("date");
                        if (dateStr != null) {
                            try {
                                String[] parts = dateStr.split("-");
                                int year = Integer.parseInt(parts[0]);
                                int month = Integer.parseInt(parts[1]);
                                int day = Integer.parseInt(parts[2]);
                                bookedDates.add(CalendarDay.from(year, month, day));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    calendarView.addDecorator(new BookedDateDecorator(bookedDates));

                    String todayStr = LocalDate.now().toString();
                    loadAppointmentsForDate(todayStr);

                    calendarView.invalidateDecorators();
                });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    date.getYear(), date.getMonth(), date.getDay());
            loadAppointmentsForDate(selectedDate);
        });
    }

    private void loadAppointmentsForDate(String date) {
        if (user == null) return;
        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(query -> {
                    appointments.clear();
                    for (DocumentSnapshot doc : query) {
                        Appointment appt = doc.toObject(Appointment.class);
                        if (appt != null) {
                            appt.setAppointmentId(doc.getId());
                            appointments.add(appt);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private static class WeekendDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            LocalDate date = LocalDate.of(day.getYear(), day.getMonth(), day.getDay());
            DayOfWeek dow = date.getDayOfWeek();
            return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
            view.addSpan(new ForegroundColorSpan(Color.LTGRAY));
        }
    }

    private static class BookedDateDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;

        BookedDateDecorator(Set<CalendarDay> dates) {
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")));
            view.addSpan(new StyleSpan(Typeface.BOLD));
        }
    }

    private class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsViewHolder> {
        private final List<Appointment> appointments;

        public AppointmentsAdapter(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        @NonNull
        @Override
        public AppointmentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.evaluator_appointment_details, parent, false);
            return new AppointmentsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppointmentsViewHolder holder, int position) {
            Appointment appt = appointments.get(position);

            holder.txtDate.setText("Date: " + appt.getDate());
            holder.txtStatus.setText("Status: " + appt.getStatus());

            if ("Pending".equalsIgnoreCase(appt.getStatus())) {
                holder.btnActions.setVisibility(View.VISIBLE);
                holder.btnActions.setText("Accept Appointment");
            } else {
                holder.btnActions.setVisibility(View.GONE);
            }

            FirebaseFirestore.getInstance().collection("children")
                    .document(appt.getChildId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String childName = doc.getString("firstName") + " " + doc.getString("lastName");
                            holder.txtChild.setText("Child: " + childName);
                        } else {
                            holder.txtChild.setText("Child: Unknown");
                        }
                    }).addOnFailureListener(e -> holder.txtChild.setText("Child: Unknown"));

            FirebaseFirestore.getInstance().collection("users")
                    .document(appt.getParentId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String parentName = doc.getString("firstName") + " " + doc.getString("lastName");
                            holder.txtParent.setText("Parent: " + parentName);
                        } else {
                            holder.txtParent.setText("Parent: Unknown");
                        }
                    }).addOnFailureListener(e -> holder.txtParent.setText("Parent: Unknown"));

            FirebaseFirestore.getInstance().collection("users")
                    .document(appt.getTherapistId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String therapistName = doc.getString("firstName") + " " + doc.getString("lastName");
                            holder.txtTherapist.setText("Therapist: " + therapistName);
                        } else {
                            holder.txtTherapist.setText("Therapist: Unknown");
                        }
                    }).addOnFailureListener(e -> holder.txtTherapist.setText("Therapist: Unknown"));

            holder.btnActions.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("appointments")
                        .document(appt.getAppointmentId())
                        .update("status", "Accepted")
                        .addOnSuccessListener(aVoid -> {
                            appt.setStatus("Accepted");
                            holder.txtStatus.setText("Status: Accepted");
                            holder.btnActions.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(holder.itemView.getContext(),
                                    "Failed to accept appointment", Toast.LENGTH_SHORT).show();
                        });
            });
        }


        @Override
        public int getItemCount() {
            return appointments.size();
        }
    }

    private void acceptAppointment(Appointment appt) {
        DocumentReference ref = db.collection("appointments").document(appt.getAppointmentId());
        ref.update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    appt.setStatus("accepted");
                    adapter.notifyDataSetChanged();
                    createFollowUpAppointment(appt);
                });
    }

    private void createFollowUpAppointment(Appointment originalAppt) {
        Appointment newAppt = new Appointment();
        newAppt.setTherapistId(originalAppt.getTherapistId());
        newAppt.setChildId(originalAppt.getChildId());
        newAppt.setDate(LocalDate.now().plusDays(7).toString());
        newAppt.setStatus("pending");

        db.collection("appointments").add(newAppt)
                .addOnSuccessListener(docRef -> {
                    newAppt.setAppointmentId(docRef.getId());
                    appointments.add(newAppt);
                    adapter.notifyDataSetChanged();
                });
    }

    private static class AppointmentsViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtTherapist, txtChild, txtParent, txtStatus;
        Button btnActions;

        public AppointmentsViewHolder(@NonNull View itemView) {
            super(itemView);
            txtParent = itemView.findViewById(R.id.txtParent);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTherapist = itemView.findViewById(R.id.txtTherapist);
            txtChild = itemView.findViewById(R.id.txtChild);
            btnActions = itemView.findViewById(R.id.btnActions);
        }
    }
}
