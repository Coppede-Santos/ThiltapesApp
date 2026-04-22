package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.ThiltapeDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ThiltapesServlet", urlPatterns = "/thiltapes")
public class ThiltapesServlet extends JsonServlet {

    private final ThiltapeDao dao = new ThiltapeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String latStr = req.getParameter("lat");
            String lngStr = req.getParameter("lng");

            if (latStr == null || lngStr == null) {
                writeJson(resp, dao.listarTodos());
                return;
            }

            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            // Retorna todos para visualizacao no mapa; o cliente aplica captura por proximidade.
            writeJson(resp, dao.listarTodos());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
