const express = require("express");
const multer = require("multer");
const path = require("path");

const env = require("../config/env");
const { createPlayer, listPlayers, updatePlayer, deletePlayer } = require("../services/playerService");
const { listAll, getById, createOne, updateOne, deleteOne } = require("../services/thiltapeService");

const router = express.Router();

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, env.UPLOADS_DIR),
  filename: (_req, file, cb) => {
    const safeOriginal = path.basename(file.originalname || "arquivo").replace(/\s+/g, "_");
    cb(null, `${Date.now()}_${safeOriginal}`);
  }
});
const upload = multer({ storage });

router.get("/admin", (_req, res) => {
  res.redirect("/thiltapes-api/admin/login");
});

router.get("/admin/login", (_req, res) => {
  res.render("login", { erro: null });
});

router.post("/admin/login", (req, res) => {
  const user = req.body?.usuario;
  const pass = req.body?.senha;

  if (user === env.ADMIN_USER && pass === env.ADMIN_PASS) {
    req.session.adminLogado = true;
    return res.redirect("/thiltapes-api/admin/dashboard");
  }

  return res.status(401).render("login", { erro: "Credenciais invalidas" });
});

router.get("/admin/dashboard", (req, res) => {
  if (!isAdmin(req)) {
    return res.redirect("/thiltapes-api/admin/login");
  }

  return renderDashboard(req, res);
});

router.post("/admin/player", async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const nome = (req.body?.nome || "").trim();
    if (!nome) {
      return redirectDashboardWith(res, "erro", "Nome do usuario e obrigatorio");
    }

    await createPlayer(nome);
    return redirectDashboardWith(res, "sucesso", "Usuario cadastrado com sucesso");
  } catch (error) {
    console.error("Failed to create player", error);
    return redirectDashboardWith(res, "erro", "Falha ao cadastrar usuario");
  }
});

router.post("/admin/player/:id/update", async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const id = Number(req.params.id);
    const nome = (req.body?.nome || "").trim();
    if (!Number.isInteger(id) || id <= 0 || !nome) {
      return redirectDashboardWith(res, "erro", "Dados invalidos para editar usuario");
    }

    const updated = await updatePlayer(id, nome);
    if (!updated) {
      return redirectDashboardWith(res, "erro", "Usuario nao encontrado");
    }

    return redirectDashboardWith(res, "sucesso", "Usuario atualizado");
  } catch (error) {
    console.error("Failed to update player", error);
    return redirectDashboardWith(res, "erro", "Falha ao atualizar usuario");
  }
});

router.post("/admin/player/:id/delete", async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const id = Number(req.params.id);
    if (!Number.isInteger(id) || id <= 0) {
      return redirectDashboardWith(res, "erro", "Usuario invalido");
    }

    const deleted = await deletePlayer(id);
    if (!deleted) {
      return redirectDashboardWith(res, "erro", "Usuario nao encontrado");
    }

    return redirectDashboardWith(res, "sucesso", "Usuario excluido");
  } catch (error) {
    console.error("Failed to delete player", error);
    return redirectDashboardWith(res, "erro", "Falha ao excluir usuario");
  }
});

router.post("/admin/thiltape", upload.single("foto"), async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const nome = (req.body?.nome || "").trim();
    const raridade = (req.body?.raridade || "").trim();
    const lat = Number(req.body?.lat);
    const lng = Number(req.body?.lng);

    if (!nome || !raridade || Number.isNaN(lat) || Number.isNaN(lng)) {
      return respondAdminError(req, res, 400, "Falha ao cadastrar thiltape");
    }

    const foto = req.file ? `/uploads/${req.file.filename}` : "";
    const created = await createOne(nome, raridade, foto, lat, lng);

    if (wantsJson(req)) {
      return res.json(created);
    }

    return res.redirect("/thiltapes-api/admin/dashboard?sucesso=Thiltape+cadastrado+com+sucesso");
  } catch (error) {
    console.error("Failed to create thiltape", error);
    return respondAdminError(req, res, 400, "Falha ao cadastrar thiltape");
  }
});

router.post("/admin/thiltape/:id/update", upload.single("foto"), async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const id = Number(req.params.id);
    const nome = (req.body?.nome || "").trim();
    const raridade = (req.body?.raridade || "").trim();
    const lat = Number(req.body?.lat);
    const lng = Number(req.body?.lng);

    if (!Number.isInteger(id) || id <= 0 || !nome || !raridade || Number.isNaN(lat) || Number.isNaN(lng)) {
      return redirectDashboardWith(res, "erro", "Dados invalidos para editar thiltape");
    }

    const atual = await getById(id);
    if (!atual) {
      return redirectDashboardWith(res, "erro", "Thiltape nao encontrado");
    }

    const foto = req.file ? `/uploads/${req.file.filename}` : (atual.foto || "");
    await updateOne(id, nome, raridade, foto, lat, lng);
    return redirectDashboardWith(res, "sucesso", "Thiltape atualizado");
  } catch (error) {
    console.error("Failed to update thiltape", error);
    return redirectDashboardWith(res, "erro", "Falha ao atualizar thiltape");
  }
});

router.post("/admin/thiltape/:id/delete", async (req, res) => {
  if (!isAdmin(req)) {
    return res.sendStatus(401);
  }

  try {
    const id = Number(req.params.id);
    if (!Number.isInteger(id) || id <= 0) {
      return redirectDashboardWith(res, "erro", "Thiltape invalido");
    }

    const deleted = await deleteOne(id);
    if (!deleted) {
      return redirectDashboardWith(res, "erro", "Thiltape nao encontrado");
    }

    return redirectDashboardWith(res, "sucesso", "Thiltape excluido");
  } catch (error) {
    console.error("Failed to delete thiltape", error);
    return redirectDashboardWith(res, "erro", "Falha ao excluir thiltape");
  }
});

function wantsJson(req) {
  const accept = req.header("accept") || "";
  return accept.includes("application/json");
}

function isAdmin(req) {
  return req.session && req.session.adminLogado === true;
}

function respondAdminError(req, res, code, message) {
  if (wantsJson(req)) {
    return res.status(code).json({ erro: message });
  }

  return res.status(code).redirect("/thiltapes-api/admin/dashboard?erro=Falha+ao+cadastrar+thiltape");
}

async function renderDashboard(req, res) {
  try {
    const [players, thiltapes] = await Promise.all([listPlayers(), listAll()]);
    return res.render("dashboard", {
      erro: req.query.erro || null,
      sucesso: req.query.sucesso || null,
      players,
      thiltapes
    });
  } catch (error) {
    console.error("Failed to render dashboard", error);
    return res.status(500).send("Falha ao carregar dashboard");
  }
}

function redirectDashboardWith(res, key, message) {
  const query = new URLSearchParams({ [key]: message });
  return res.redirect(`/thiltapes-api/admin/dashboard?${query.toString()}`);
}

module.exports = router;
