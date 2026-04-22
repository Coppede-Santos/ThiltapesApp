package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.CaptureDao;
import com.thiltapes.api.dao.ThiltapeDao;
import com.thiltapes.api.model.Thiltape;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

@WebServlet(name = "CaptureServlet", urlPatterns = "/capturar")
public class CaptureServlet extends JsonServlet {

    private final CaptureDao captureDao = new CaptureDao();
    private final ThiltapeDao thiltapeDao = new ThiltapeDao();

    private static class CaptureRequest {
        int playerId;
        int thiltapeId;
        Double lat;
        Double lng;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            CaptureRequest body = readJsonBody(req, CaptureRequest.class);
            if (body == null || body.playerId <= 0 || body.thiltapeId <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            List<Thiltape> todos = thiltapeDao.listarTodos();
            boolean existe = todos.stream().anyMatch(t -> t.getId() == body.thiltapeId);
            if (!existe) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (body.lat != null && body.lng != null) {
                List<Thiltape> proximos = thiltapeDao.listarPorDistancia(body.lat, body.lng, 100);
                boolean dentroDoRaio = proximos.stream().anyMatch(t -> t.getId() == body.thiltapeId);
                if (!dentroDoRaio) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    writeJson(resp, Map.of("erro", "Thiltape fora do raio de captura (100m)"));
                    return;
                }
            }

            boolean capturado = captureDao.capturar(body.playerId, body.thiltapeId);
            writeJson(resp, Map.of("capturado", capturado));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
