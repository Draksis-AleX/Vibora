package com.example.vibora.model;

public class UserMatchResultModel {
    String userId;
    String username;
    String result;

    public UserMatchResultModel() {
    }

    public UserMatchResultModel(String userId, String username, String result) {
        this.userId = userId;
        this.username = username;
        this.result = result;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
