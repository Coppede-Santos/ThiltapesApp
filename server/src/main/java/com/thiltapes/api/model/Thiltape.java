package com.thiltapes.api.model;

public class Thiltape {
    private int id;
    private String nome;
    private String raridade;
    private String foto;
    private double lat;
    private double lng;

    public Thiltape(int id, String nome, String raridade, String foto, double lat, double lng) {
        this.id = id;
        this.nome = nome;
        this.raridade = raridade;
        this.foto = foto;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRaridade() {
        return raridade;
    }

    public String getFoto() {
        return foto;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
