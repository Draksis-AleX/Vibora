package com.example.vibora.model;

import com.google.firebase.Timestamp;

public class ReportModel {
    String userId;
    String username;
    Timestamp date;
    String reason;
    String reporterId;
    String reporterUsername;

    public ReportModel() {
    }

    public ReportModel(String userId, String username, Timestamp date, String reason, String reporterId, String reporterUsername) {
        this.userId = userId;
        this.username = username;
        this.date = date;
        this.reason = reason;
        this.reporterId = reporterId;
        this.reporterUsername = reporterUsername;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public void setReporterUsername(String reporterUsername) {
        this.reporterUsername = reporterUsername;
    }
}
