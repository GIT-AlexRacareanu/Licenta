package com.example.abatherapy.Models;

import java.io.Serializable;

public class Children implements Serializable {

    public Children(){
    }
    public Children(String age, String birthDate, String firstName, String lastName, String cnp, String childId, String parentId) {
        this.age = age;
        this.birthDate = birthDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cnp = cnp;
        this.childId = childId;
        this.parentId = parentId;
    }
    private String age;
    private String birthDate;
    private String firstName;
    private String lastName;
    private String cnp;
    private String childId;
    private String parentId;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public String getAge() {
        return age;
    }
    public String getBirthDate() {
        return birthDate;
    }
    public String getCnp() {
        return cnp;
    }
    public String getChildId() {
        return childId;
    }
    public String getParentId() {
        return parentId;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setCnp(String cnp) {
        this.cnp = cnp;
    }
    public void setChildId(String childId) {
        this.childId = childId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName;
    }
}
