package com.thiltapes.api.servlet;

import com.thiltapes.api.dao.ThiltapeDao;
import com.thiltapes.api.model.Thiltape;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "AdminThiltapeServlet", urlPatterns = "/admin/thiltape")
@MultipartConfig
public class AdminThiltapeServlet extends HttpServlet {

    private final ThiltapeDao dao = new ThiltapeDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (!isAdminLogado(req)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String nome = req.getParameter("nome");
            String raridade = req.getParameter("raridade");
            double lat = Double.parseDouble(req.getParameter("lat"));
            double lng = Double.parseDouble(req.getParameter("lng"));

            Part fotoPart = req.getPart("foto");
            String fotoRelativa = salvarFoto(req, fotoPart);

            Thiltape novo = dao.create(nome, raridade, fotoRelativa, lat, lng);

            String accept = req.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                resp.setContentType("application/json");
                resp.getWriter().write(new com.google.gson.Gson().toJson(novo));
                return;
            }

            req.setAttribute("sucesso", "Thiltape cadastrado com sucesso!");
            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            String accept = req.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write(new com.google.gson.Gson().toJson(Map.of("erro", "Falha ao cadastrar thiltape")));
                return;
            }

            req.setAttribute("erro", "Falha ao cadastrar thiltape.");
            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
        }
    }

    private String salvarFoto(HttpServletRequest req, Part fotoPart) throws IOException {
        if (fotoPart == null || fotoPart.getSize() == 0) {
            return "";
        }

        String uploadsPath = req.getServletContext().getRealPath("/uploads");
        File dir = new File(uploadsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String original = Path.of(fotoPart.getSubmittedFileName()).getFileName().toString();
        String filename = UUID.randomUUID() + "_" + original;
        Path destino = Path.of(uploadsPath, filename);
        Files.copy(fotoPart.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }

    private boolean isAdminLogado(HttpServletRequest req) {
        Object attr = req.getSession(true).getAttribute("adminLogado");
        return attr instanceof Boolean && (Boolean) attr;
    }
}
