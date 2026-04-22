package com.example.thiltapeshunting.model;

public class GameStatus {
    private final int playerId;
    private final int totalThiltapes;
    private final int totalCapturados;
    private final boolean finalizada;

    public GameStatus(int playerId, int totalThiltapes, int totalCapturados, boolean finalizada) {
        this.playerId = playerId;
        this.totalThiltapes = totalThiltapes;
        this.totalCapturados = totalCapturados;
        this.finalizada = finalizada;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getTotalThiltapes() {
        return totalThiltapes;
    }

    public int getTotalCapturados() {
        return totalCapturados;
    }

    public boolean isFinalizada() {
        return finalizada;
    }
}
