package com.example.abatherapy.Activities.Patient;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abatherapy.Children;
import com.example.abatherapy.Models.User;
import com.example.abatherapy.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PatientProfileFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;
    private TextView txtFullName, txtEmail, txtPhone, txtAdress, txtRole, txtExperience, txtDescription;
    private RecyclerView recyclerChildren;
    private ChildrenAdapter adapter;
    private List<Children> childrenList = new ArrayList<>();
    private FirebaseFirestore db;

    public PatientProfileFragment() {}

    public static PatientProfileFragment newInstance(User user) {
        PatientProfileFragment fragment = new PatientProfileFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.patient_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtAdress = view.findViewById(R.id.txtAdress);
        txtRole = view.findViewById(R.id.txtRole);
        txtExperience = view.findViewById(R.id.txtExperience);

        recyclerChildren = view.findViewById(R.id.recyclerChildren);

        recyclerChildren.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChildrenAdapter(childrenList);
        recyclerChildren.setAdapter(adapter);

        FloatingActionButton fabAddChild = view.findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(v -> showAddChildDialog());

        if (user != null) {
            txtFullName.setText(user.getFirstName() + " " + user.getLastName());
            txtEmail.setText("email: " + user.getEmail());
            txtPhone.setText("phone: " + user.getPhone());
            txtAdress.setText("address: " + user.getAddress());
            txtRole.setText(user.getRole());
            txtExperience.setText("experience: " + user.getExperienta());
            loadChildren(user.getUid());
        }
    }

    private void showAddChildDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.add_child_dialog, null);
        EditText editLastName = dialogView.findViewById(R.id.editLastName);
        EditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        EditText editAge = dialogView.findViewById(R.id.editAge);
        EditText editBirthDate = dialogView.findViewById(R.id.editBirthDate);
        EditText editCnp = dialogView.findViewById(R.id.editCnp);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Child")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String firstName = editFirstName.getText().toString().trim();
                    String lastName = editLastName.getText().toString().trim();
                    String age = editAge.getText().toString().trim();
                    String birthDate = editBirthDate.getText().toString().trim();
                    String cnp = editCnp.getText().toString().trim();

                    if (firstName.isEmpty() || lastName.isEmpty()) {
                        Toast.makeText(getContext(), "Nume și prenume sunt obligatorii", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String childId = db.collection("children").document().getId(); // generate new ID
                    Children newChild = new Children();
                    newChild.setChildId(childId);
                    newChild.setFirstName(firstName);
                    newChild.setLastName(lastName);
                    newChild.setAge(age);
                    newChild.setBirthDate(birthDate);
                    newChild.setCnp(cnp);
                    newChild.setParentId(user.getUid());

                    db.collection("children")
                            .document(childId)  // use the generated ID
                            .set(newChild)      // save with .set() instead of .add()
                            .addOnSuccessListener(aVoid -> {
                                childrenList.add(newChild);
                                adapter.notifyItemInserted(childrenList.size() - 1);
                                Toast.makeText(getContext(), "Copil adăugat cu succes", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Eroare la adăugarea copilului", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Eroare la încărcarea copiilor", Toast.LENGTH_SHORT).show());
    }

    private static class ChildrenAdapter extends RecyclerView.Adapter<ChildrenViewHolder> {
        private final List<Children> children;

        ChildrenAdapter(List<Children> children) {
            this.children = children;
        }

        @NonNull
        @Override
        public ChildrenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_item_child, parent, false);
            return new ChildrenViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChildrenViewHolder holder, int position) {
            Children child = children.get(position);
            holder.bind(child);  // pass the full object
        }


        @Override
        public int getItemCount() {
            return children.size();
        }
    }

    private static class ChildrenViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtAge;

        public ChildrenViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtChildName);
            txtAge = itemView.findViewById(R.id.txtChildAge);
        }

        public void bind(Children child) {
            txtName.setText(child.getFirstName() + " " + child.getLastName());
            txtAge.setText("Age: " + child.getAge());

            itemView.setOnClickListener(v -> {
                showChildDialog(v.getContext(), child);
            });
        }

        private void showChildDialog(Context context, Children child) {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle(child.getFirstName() + " " + child.getLastName())
                    .setMessage(
                            "Age: " + child.getAge() + "\n" +
                                    "CNP: " + child.getCnp() + "\n" +
                                    "Birth Date: " + child.getBirthDate()
                    )
                    .setPositiveButton("Close", null)
                    .show();
        }
    }

}
