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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TherapistHomeFragment extends Fragment {

    private User user;
    private RecyclerView recyclerTodayAppointments;
    private SimpleStringAdapter adapter;
    private ArrayList<String> todayAppointments = new ArrayList<>();

    public static TherapistHomeFragment newInstance(User user) {
        TherapistHomeFragment fragment = new TherapistHomeFragment();
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
        return inflater.inflate(R.layout.therapist_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtGreeting = view.findViewById(R.id.txtGreeting);
        recyclerTodayAppointments = view.findViewById(R.id.recyclerTodayAppointments);

        if (user != null) {
            txtGreeting.setText("Hello, " + user.getFirstName() + "!");
        }

        recyclerTodayAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SimpleStringAdapter(todayAppointments);
        recyclerTodayAppointments.setAdapter(adapter);

        loadTodayAppointments(); // <-- Important
    }

    private void loadTodayAppointments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid())
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todayAppointments.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        todayAppointments.add("No appointments today");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String childId = doc.getString("childId");
                        String status = doc.getString("status");

                        db.collection("children").document(childId).get()
                                .addOnSuccessListener(childDoc -> {
                                    String childName = childDoc.exists()
                                            ? childDoc.getString("name")
                                            : "Unknown Child";

                                    todayAppointments.add(childName + " (" + status + ")");
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    todayAppointments.clear();
                    todayAppointments.add("Error loading appointments");
                    adapter.notifyDataSetChanged();
                });
    }

    private static class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.ViewHolder> {
        private final List<String> items;

        SimpleStringAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
