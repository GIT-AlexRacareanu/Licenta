package com.example.abatherapy;

public class Appointment {
    private String date;
    private String description;
    private String parentId;
    private String status;
    private String therapistId;

    public Appointment(String date, String description, String parentId, String status, String therapistId) {
        this.date = date;
        this.description = description;
        this.parentId = parentId;
        this.status = status;
        this.therapistId = therapistId;
    }

    public String getDate() {
        return date;
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
    public void setDate(String date) {
        this.date = date;
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
