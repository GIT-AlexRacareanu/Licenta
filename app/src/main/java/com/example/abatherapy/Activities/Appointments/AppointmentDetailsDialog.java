package com.example.abatherapy.Activities.Appointments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.abatherapy.Models.Appointment;
import com.example.abatherapy.R;

public class AppointmentDetailsDialog extends DialogFragment {

    private static final String ARG_APPT = "appointment";
    private Appointment appointment;

    public static AppointmentDetailsDialog newInstance(Appointment appt) {
        AppointmentDetailsDialog dialog = new AppointmentDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_APPT, appt);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            appointment = (Appointment) getArguments().getSerializable(ARG_APPT);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Appointment Details");

        View view = getLayoutInflater().inflate(R.layout.evaluator_appointment_details, null);

        TextView txtDate = view.findViewById(R.id.txtDate);
        TextView txtChild = view.findViewById(R.id.txtChild);
        TextView txtParent = view.findViewById(R.id.txtParent);
        TextView txtStatus = view.findViewById(R.id.txtStatus);
        Button btnAction = view.findViewById(R.id.btnActions);

        txtDate.setText("Date: " + appointment.getDate());
        txtChild.setText("Child ID: " + appointment.getChildId());
        txtParent.setText("Parent ID: " + appointment.getParentId());
        txtStatus.setText("Status: " + appointment.getStatus());

        btnAction.setOnClickListener(v -> {
        });

        builder.setView(view);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}