package com.example.abatherapy.Database;

import com.example.abatherapy.Children;
import com.example.abatherapy.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class DBManager {

    private static FirebaseDatabase dbRef;
    private DBManager()
    {
        DBManager.dbRef = FirebaseDatabase.getInstance();
    };

    public static FirebaseDatabase getDB() {
        if (DBManager.dbRef == null) {
            DBManager.dbRef = FirebaseDatabase.getInstance();
        }
        return DBManager.dbRef;
    }

    public static DatabaseReference getUsers()
    {
        return DBManager.getDB().getReference("users");
    }

    public interface ChildrenCallback {
        void onCallback(ArrayList<Children> childrenList);
    }
    public static void getChildren(User user, ChildrenCallback callback) {
        Query query = FirebaseDatabase.getInstance()
                .getReference("children")
                .orderByChild("parentId")
                .equalTo(user.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Children> childrenList = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Children child = childSnapshot.getValue(Children.class);
                    if (child != null) childrenList.add(child);
                }
                callback.onCallback(childrenList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    public interface OnAppointmentsLoaded {
        void onLoaded(ArrayList<String> dates);
    }

    public static void getAppointments(User user, OnAppointmentsLoaded callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("therapistId", user.getUid()) // filter by evaluator UID
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> dates = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String date = doc.getString("date"); // must match your Firestore field
                        if (date != null) {
                            dates.add(date);
                        }
                    }
                    callback.onLoaded(dates);
                })
                .addOnFailureListener(e -> {
                    callback.onLoaded(new ArrayList<>()); // return empty if error
                });
    }
}
