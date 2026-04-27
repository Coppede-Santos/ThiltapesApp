<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard Admin - Cacando Thiltapes</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <style>
        body { font-family: Arial, sans-serif; background: #eef3f6; margin: 0; padding: 20px; }
        .wrap { max-width: 760px; margin: 0 auto; background: #fff; border-radius: 14px; box-shadow: 0 10px 28px rgba(0,0,0,.07); padding: 24px; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        input, select { width: 100%; padding: 10px; margin-top: 6px; }
        button { margin-top: 14px; background: #0a6ea8; color: #fff; padding: 12px 20px; border: 0; border-radius: 8px; }
        .ok { color: #156c2f; }
        .erro { color: #b00020; }
    </style>
</head>
<body>
<div class="wrap">
    <h2>Cadastro de Thiltape</h2>

    <% if (request.getAttribute("sucesso") != null) { %>
    <p class="ok"><%= request.getAttribute("sucesso") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
    <p class="erro"><%= request.getAttribute("erro") %></p>
    <% } %>

    <form method="post" enctype="multipart/form-data" action="<%= request.getContextPath() %>/admin/thiltape">
        <div class="grid">
            <div>
                <label>Nome do professor</label>
                <input type="text" name="nome" required/>
            </div>
            <div>
                <label>Raridade</label>
                <select name="raridade" required>
                    <option value="Comum">Comum</option>
                    <option value="Raro">Raro</option>
                    <option value="Lendario">Lendario</option>
                </select>
            </div>
            <div>
                <label>Latitude</label>
                <input type="number" step="any" name="lat" required/>
            </div>
            <div>
                <label>Longitude</label>
                <input type="number" step="any" name="lng" required/>
            </div>
        </div>

        <label>Foto</label>
        <input type="file" name="foto" accept="image/*" capture="environment" required/>

        <button type="submit">Salvar Thiltape</button>
    </form>
</div>
</body>
</html>
