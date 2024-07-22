package com.example.vibora.model;

public class PlayerResult {
    private String playerId;
    private String result;

    public PlayerResult() {
    }

    public PlayerResult(String playerId, String result) {
        this.playerId = playerId;
        this.result = result;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
