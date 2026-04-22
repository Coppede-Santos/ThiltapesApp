package com.thiltapes.api.dao;

import com.thiltapes.api.db.Database;
import com.thiltapes.api.model.Thiltape;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ThiltapeDao {

    public List<Thiltape> listarTodos() throws Exception {
        String sql = "SELECT id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng FROM thiltape";
        List<Thiltape> itens = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                itens.add(new Thiltape(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("raridade"),
                        rs.getString("foto"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ));
            }
        }

        return itens;
    }

    public List<Thiltape> listarPorDistancia(double lat, double lng, int distanciaEmMetros) throws Exception {
        String sql = "SELECT id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng " +
                "FROM thiltape " +
                "WHERE ST_Distance(localizacao, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) <= ?";

        List<Thiltape> itens = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, lng);
            ps.setDouble(2, lat);
            ps.setInt(3, distanciaEmMetros);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    itens.add(new Thiltape(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("raridade"),
                            rs.getString("foto"),
                            rs.getDouble("lat"),
                            rs.getDouble("lng")
                    ));
                }
            }
        }
        return itens;
    }

    public Thiltape create(String nome, String raridade, String foto, double lat, double lng) throws Exception {
        String sql = "INSERT INTO thiltape (nome, raridade, foto, localizacao) VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) " +
                "RETURNING id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, raridade);
            ps.setString(3, foto);
            ps.setDouble(4, lng);
            ps.setDouble(5, lat);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Thiltape(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("raridade"),
                            rs.getString("foto"),
                            rs.getDouble("lat"),
                            rs.getDouble("lng")
                    );
                }
            }
        }

        return null;
    }

    public int contarTodos() throws Exception {
        String sql = "SELECT COUNT(*) FROM thiltape";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
