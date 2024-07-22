package com.example.vibora.model;

import com.example.vibora.utils.CalendarUtils;
import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.util.ArrayList;

public class BookingModel {

    private String fieldId;
    private ArrayList<String> userIdList;
    private ArrayList<PlayerResult> matchResults;
    private Timestamp date;
    private int timeSlot;
    private boolean isReserved;

    public BookingModel() {
    }

    public BookingModel(String fieldName, LocalDate date, int timeSlot, String userId) {
        this.fieldId = fieldName;
        this.date = CalendarUtils.convertFromLocalDateToTimestamp(date);
        this.timeSlot = timeSlot;
        if(userIdList == null) userIdList = new ArrayList<String>();
        if(matchResults == null) matchResults = new ArrayList<PlayerResult>();
        this.userIdList.add(userId);
        this.matchResults.add(new PlayerResult(userId, "?"));
        this.isReserved = false;
    }

    public BookingModel(String fieldName, LocalDate date, int timeSlot, ArrayList<String> userIdList, ArrayList<PlayerResult> matchResults) {
        this.fieldId = fieldName;
        this.date = CalendarUtils.convertFromLocalDateToTimestamp(date);
        this.timeSlot = timeSlot;
        if(userIdList == null) userIdList = new ArrayList<String>();
        if(matchResults == null) matchResults = new ArrayList<PlayerResult>();
        this.userIdList = userIdList;
        this.matchResults = matchResults;
        this.isReserved = true;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public ArrayList<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(ArrayList<String> userIdList) {
        this.userIdList = userIdList;
    }

    public ArrayList<PlayerResult> getMatchResults() {
        return matchResults;
    }

    public void setMatchResults(ArrayList<PlayerResult> matchResults) {
        this.matchResults = matchResults;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }
}
