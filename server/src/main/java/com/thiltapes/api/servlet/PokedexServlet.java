package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.CaptureDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "PokedexServlet", urlPatterns = "/pokedex")
public class PokedexServlet extends JsonServlet {

    private final CaptureDao captureDao = new CaptureDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String playerIdStr = req.getParameter("playerId");
            if (playerIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int playerId = Integer.parseInt(playerIdStr);
            writeJson(resp, captureDao.listarPokedex(playerId));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
