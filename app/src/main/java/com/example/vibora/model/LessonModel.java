package com.example.vibora.model;

import com.google.firebase.Timestamp;

public class LessonModel {
    String field_name;
    Timestamp date;
    int timeslot;
    String teacherId;
    String userId;
    boolean isBooked;

    public LessonModel() {
    }

    public LessonModel(String field_name, Timestamp date, int timeslot, String teacherId) {
        this.field_name = field_name;
        this.date = date;
        this.timeslot = timeslot;
        this.teacherId = teacherId;
        this.isBooked = false;
    }

    public String getField_name() {
        return field_name;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}
