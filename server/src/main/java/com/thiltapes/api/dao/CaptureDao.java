package com.thiltapes.api.dao;

import com.thiltapes.api.db.Database;
import com.thiltapes.api.model.Thiltape;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CaptureDao {

    public boolean capturar(int playerId, int thiltapeId) throws Exception {
        String sql = "INSERT INTO captura (player_id, thiltape_id) VALUES (?, ?) ON CONFLICT (player_id, thiltape_id) DO NOTHING";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, thiltapeId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Thiltape> listarPokedex(int playerId) throws Exception {
        String sql = "SELECT t.id, t.nome, t.raridade, t.foto, ST_Y(t.localizacao::geometry) AS lat, ST_X(t.localizacao::geometry) AS lng " +
                "FROM captura c JOIN thiltape t ON t.id = c.thiltape_id WHERE c.player_id = ? ORDER BY c.data DESC";
        List<Thiltape> itens = new ArrayList<>();

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
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

    public int contarCapturadosPorPlayer(int playerId) throws Exception {
        String sql = "SELECT COUNT(*) FROM captura WHERE player_id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
