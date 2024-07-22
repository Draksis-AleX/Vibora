package com.example.vibora.model;

public class UserModel {
    private String full_name;
    private String username;
    private String email;
    private String userId;
    private int skill_rating;
    private int isAdmin;
    private int isBanned;
    private String profilePic;

    public UserModel() {
    }

    public UserModel(String full_name, String username, String email, int skill_rating, String userId) {
        this.full_name = full_name;
        this.username = username;
        this.skill_rating = skill_rating;
        this.email = email;
        this.userId = userId;
        this.isAdmin = 0;
        this.isBanned = 0;
    }

    public UserModel(String full_name, String username, String email, int skill_rating, String userId, int isAdmin) {
        this.full_name = full_name;
        this.username = username;
        this.skill_rating = skill_rating;
        this.email = email;
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.isBanned = 0;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail(){ return email; }

    public void setEmail(String email){ this.email = email; }

    public int getSkill_rating() {
        return skill_rating;
    }

    public void setSkill_rating(int skill_rating) {
        this.skill_rating = skill_rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getIsBanned() {
        return isBanned;
    }

    public void setBanned(int banned) {
        this.isBanned = banned;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
