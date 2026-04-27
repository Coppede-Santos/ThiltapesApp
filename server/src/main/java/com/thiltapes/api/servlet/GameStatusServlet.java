package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.CaptureDao;
import com.thiltapes.api.dao.ThiltapeDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@WebServlet(name = "GameStatusServlet", urlPatterns = "/status-partida")
public class GameStatusServlet extends JsonServlet {

    private final CaptureDao captureDao = new CaptureDao();
    private final ThiltapeDao thiltapeDao = new ThiltapeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String playerIdStr = req.getParameter("playerId");
            if (playerIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(resp, Map.of("erro", "Parametro playerId eh obrigatorio"));
                return;
            }

            int playerId = Integer.parseInt(playerIdStr);
            int totalThiltapes = thiltapeDao.contarTodos();
            int totalCapturados = captureDao.contarCapturadosPorPlayer(playerId);
            boolean finalizada = totalThiltapes > 0 && totalCapturados >= totalThiltapes;

            writeJson(resp, Map.of(
                    "playerId", playerId,
                    "totalThiltapes", totalThiltapes,
                    "totalCapturados", totalCapturados,
                    "finalizada", finalizada
            ));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
