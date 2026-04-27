<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login Admin - Cacando Thiltapes</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <style>
        body { font-family: Arial, sans-serif; background: #f2f4f8; margin: 0; }
        .card { max-width: 400px; margin: 80px auto; background: #fff; padding: 24px; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,.08); }
        input { width: 100%; padding: 10px; margin-top: 8px; margin-bottom: 16px; }
        button { width: 100%; padding: 12px; background: #007c6d; color: #fff; border: 0; border-radius: 8px; }
        .erro { color: #b00020; margin-bottom: 12px; }
    </style>
</head>
<body>
<div class="card">
    <h2>Admin - Cacando Thiltapes</h2>
    <% if (request.getAttribute("erro") != null) { %>
    <div class="erro"><%= request.getAttribute("erro") %></div>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/admin/login">
        <label>Usuario</label>
        <input type="text" name="usuario" required/>
        <label>Senha</label>
        <input type="password" name="senha" required/>
        <button type="submit">Entrar</button>
    </form>
</div>
</body>
</html>
