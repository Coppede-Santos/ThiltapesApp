package com.thiltapes.api.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AdminLoginServlet", urlPatterns = "/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final String ADMIN_USER = System.getenv().getOrDefault("ADMIN_USER", "admin");
    private static final String ADMIN_PASS = System.getenv().getOrDefault("ADMIN_PASS", "admin123");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String user = req.getParameter("usuario");
        String pass = req.getParameter("senha");

        if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
            req.getSession(true).setAttribute("adminLogado", true);
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        req.setAttribute("erro", "Credenciais invalidas");
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }
}
