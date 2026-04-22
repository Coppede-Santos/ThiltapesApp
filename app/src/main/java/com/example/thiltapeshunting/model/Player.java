package com.example.thiltapeshunting.model;

public class Player {
    public int id;
    public String nome;
    public int capturas;

    public Player(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Player(int id, String nome, int capturas) {
        this.id = id;
        this.nome = nome;
        this.capturas = capturas;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
