package com.thiltapes.api.servlet;

import com.google.gson.JsonObject;
import com.thiltapes.api.dao.PlayerDao;
import com.thiltapes.api.model.Player;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PlayerServlet", urlPatterns = "/player")
public class PlayerServlet extends JsonServlet {

    private final PlayerDao playerDao = new PlayerDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            JsonObject body = gson.fromJson(readBody(req), JsonObject.class);
            if (body == null || !body.has("nome") || body.get("nome").isJsonNull() || body.get("nome").getAsString().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Player player = playerDao.create(body.get("nome").getAsString().trim());
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"id\":" + player.getId() + ",\"nome\":\"" + player.getNome().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            getServletContext().log("Failed to create player", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
