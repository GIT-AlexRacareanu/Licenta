package com.example.abatherapy.Models;

import android.util.Log;

import com.example.abatherapy.UserCallback;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;

public class User implements Serializable {

    public User(){
    }
    public User(String address, String descriere, String email, boolean evaluator, String experienta, String firstName, String lastName, String phone, String role, String specializare, String uid) {
        this.address = address;
        this.descriere = descriere;
        this.email = email;
        this.evaluator = evaluator;
        this.experienta = experienta;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.specializare = specializare;
        this.uid = uid;
    }

    private String address = "";
    private String descriere = "";
    private String email = "";
    private boolean evaluator = false;
    private String experienta = "";
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String role = "";
    private String specializare = "";
    private String uid = "";

    public String getAddress() {
        return address;
    }

    public String getDescriere() {
        return descriere;
    }
    public String getEmail() {
        return email;
    }

    public String getExperienta() {
        return experienta;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getSpecializare() {
        return specializare;
    }

    public String getUid() {
        return uid;
    }

    public boolean isEvaluator() {
        return evaluator;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setEvaluator(boolean evaluator) {
        this.evaluator = evaluator;
    }
    public void setExperienta(String experienta) {
        this.experienta = experienta;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public void setSpecializare(String specializare) {
        this.specializare = specializare;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public static void databaseMapper(FirebaseUser dbUser, UserCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = dbUser.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");
                        String role = document.getString("role");
                        String specializare = document.getString("specializare");
                        String experienta = document.getString("experienta");
                        String descriere = document.getString("descriere");
                        String address = document.getString("address");
                        boolean evaluator = document.getBoolean("evaluator");
                        User user = new User(
                                address,
                                descriere,
                                email,
                                evaluator,
                                experienta,
                                firstName,
                                lastName,
                                phone,
                                role,
                                specializare,
                                document.getId()
                        );
                        callback.onUserLoaded(user);
                    } else {
                        callback.onUserLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading user", e);
                    callback.onUserLoaded(null);
                });
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName;
    }
}
