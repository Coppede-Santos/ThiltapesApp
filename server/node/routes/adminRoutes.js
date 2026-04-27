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

function wantsJson(req) {
  const accept = req.header("accept") || "";
  const contentType = req.header("content-type") || "";
  return accept.includes("application/json") || contentType.includes("application/json");
}

function isAdmin(req) {
  // Se for uma chamada JSON (do App Android), permitimos para facilitar o desenvolvimento
  if (wantsJson(req)) return true;
  // Se for via navegador, mantemos a sessão para segurança da dashboard web
  return req.session && req.session.adminLogado === true;
}

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

// LISTAR PLAYERS (Aberto para o App)
router.get("/admin/players", async (req, res) => {
  try {
    const players = await listPlayers();
    res.json(players);
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

// CRIAR PLAYER (Aberto para o App)
router.post("/admin/player", async (req, res) => {
  try {
    const nome = (req.body?.nome || "").trim();
    if (!nome) return res.status(400).json({ erro: "Nome obrigatorio" });
    const player = await createPlayer(nome);
    if (wantsJson(req)) return res.json(player);
    return res.redirect("/thiltapes-api/admin/dashboard?sucesso=Usuario+cadastrado");
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

// LISTAR THILTAPES (Aberto para o App)
router.get("/admin/thiltapes", async (req, res) => {
  try {
    const thiltapes = await listAll();
    res.json(thiltapes);
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

// CRIAR THILTAPE (Aberto para o App)
router.post("/admin/thiltape", upload.single("foto"), async (req, res) => {
  try {
    const { nome, raridade, lat, lng } = req.body;
    if (!nome || !raridade || lat === undefined || lng === undefined) {
      return res.status(400).json({ erro: "Campos obrigatorios faltando" });
    }
    const foto = req.file ? `/uploads/${req.file.filename}` : "";
    const created = await createOne(nome, raridade, foto, Number(lat), Number(lng));
    if (wantsJson(req)) return res.json(created);
    return res.redirect("/thiltapes-api/admin/dashboard?sucesso=Thiltape+cadastrado");
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

// RANKING (Aberto para o App)
router.get("/admin/ranking", async (req, res) => {
  try {
    const players = await listPlayers();
    res.json(players);
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

// DELETE THILTAPE
router.delete("/admin/thiltapes/:id", async (req, res) => {
  try {
    const deleted = await deleteOne(Number(req.params.id));
    res.sendStatus(deleted ? 200 : 404);
  } catch (error) {
    res.status(500).json({ erro: error.message });
  }
});

async function renderDashboard(req, res) {
  try {
    const [players, thiltapes] = await Promise.all([listPlayers(), listAll()]);
    res.render("dashboard", {
      erro: req.query.erro || null,
      sucesso: req.query.sucesso || null,
      players,
      thiltapes
    });
  } catch (error) {
    res.status(500).send("Falha ao carregar dashboard");
  }
}

module.exports = router;
