package com.thiltapes.api.dao;

import com.thiltapes.api.db.Database;
import com.thiltapes.api.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerDao {

    public Player create(String nome) throws Exception {
        String sql = "INSERT INTO player (nome) VALUES (?) RETURNING id, nome";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Player(rs.getInt("id"), rs.getString("nome"));
                }
            }
        }
        return null;
    }
}
