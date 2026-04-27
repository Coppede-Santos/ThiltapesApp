package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.ThiltapeDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminListThiltapesServlet", urlPatterns = "/admin/thiltapes")
public class AdminListThiltapesServlet extends JsonServlet {

    private final ThiltapeDao dao = new ThiltapeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdminLogado(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            writeJson(resp, dao.listarTodos());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isAdminLogado(HttpServletRequest req) {
        Object attr = req.getSession(true).getAttribute("adminLogado");
        return attr instanceof Boolean && (Boolean) attr;
    }
}
