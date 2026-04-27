package com.thiltapes.api.model;

public class Player {
    private int id;
    private String nome;

    public Player(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
