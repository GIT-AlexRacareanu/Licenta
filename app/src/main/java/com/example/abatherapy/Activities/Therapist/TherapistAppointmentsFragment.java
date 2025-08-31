package com.example.abatherapy.Activities.Therapist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TherapistAppointmentsFragment extends Fragment {

    private User user;
    private RecyclerView recyclerAppointments;
    private AppointmentAdapter adapter;
    private List<AppointmentItem> appointments = new ArrayList<>();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.therapist_appointments_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerAppointments = view.findViewById(R.id.recyclerAppointments);
        recyclerAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppointmentAdapter(appointments);
        recyclerAppointments.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointments.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String childId = doc.getString("childId");
                        String date = doc.getString("date");
                        String status = doc.getString("status");

                        // Fetch child name
                        db.collection("children").document(childId).get()
                                .addOnSuccessListener(childDoc -> {
                                    String childName = childDoc.exists() ? childDoc.getString("name") : "Unknown Child";

                                    appointments.add(new AppointmentItem(childName, date, status));
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    appointments.clear();
                    appointments.add(new AppointmentItem("Error", "N/A", "Could not load"));
                    adapter.notifyDataSetChanged();
                });
    }

    private static class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
        private final List<AppointmentItem> items;

        AppointmentAdapter(List<AppointmentItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.therapist_item_appointment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AppointmentItem item = items.get(position);
            holder.txtChildName.setText(item.childName);
            holder.txtDate.setText(item.date);
            holder.txtStatus.setText(item.status);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtChildName, txtDate, txtStatus;
            ViewHolder(View itemView) {
                super(itemView);
                txtChildName = itemView.findViewById(R.id.txtChildName);
                txtDate = itemView.findViewById(R.id.txtDate);
                txtStatus = itemView.findViewById(R.id.txtStatus);
            }
        }
    }

    private static class AppointmentItem {
        String childName, date, status;

        AppointmentItem(String childName, String date, String status) {
            this.childName = childName;
            this.date = date;
            this.status = status;
        }
    }
}
