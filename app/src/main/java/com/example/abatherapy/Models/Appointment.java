package com.example.abatherapy.Models;

import java.io.Serializable;

public class Appointment implements Serializable {

    private String appointmentId;
    private String childId;
    private String date;
    private String time;
    private String description;
    private String parentId;
    private String status;
    private String therapistId;

    public Appointment(){}
    public Appointment(String appointmentId,String childId, String date, String time, String description, String parentId, String status, String therapistId) {
        this.appointmentId = appointmentId;
        this.childId = childId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.parentId = parentId;
        this.status = status;
        this.therapistId = therapistId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }
    public String getChildId() {
        return childId;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getDescription() {
        return description;
    }
    public String getParentId() {
        return parentId;
    }
    public String getStatus() {
        return status;
    }
    public String getTherapistId() {
        return therapistId;
    }
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    public void setChildId(String childId) {
        this.childId = childId;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }
}
