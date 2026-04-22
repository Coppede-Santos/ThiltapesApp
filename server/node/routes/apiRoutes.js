const express = require("express");

const env = require("../config/env");
const { createPlayer } = require("../services/playerService");
const {
  listAll,
  listNearby,
  ensureSpawnForPlayer,
  seedNearby,
  countAll
} = require("../services/thiltapeService");
const {
  existsPlayer,
  existsThiltape,
  inCaptureRange,
  capture,
  listPokedex,
  countCaptured
} = require("../services/captureService");

const router = express.Router();

router.get("/health", (_req, res) => {
  res.json({ ok: true });
});

router.post("/player", async (req, res) => {
  const nome = (req.body?.nome || "").trim();
  if (!nome) {
    return res.sendStatus(400);
  }

  try {
    const player = await createPlayer(nome);
    return res.json(player);
  } catch (error) {
    console.error("Failed to create player", error);
    return res.sendStatus(500);
  }
});

router.get("/thiltapes", async (req, res) => {
  try {
    const lat = Number(req.query.lat);
    const lng = Number(req.query.lng);
    const playerId = Number(req.query.playerId);
    const hasCoords = !Number.isNaN(lat) && !Number.isNaN(lng);

    if (!hasCoords) {
      const rows = await listAll();
      return res.json(rows);
    }

    if (!Number.isNaN(playerId) && playerId > 0 && env.AUTO_SPAWN_NEARBY) {
      await ensureSpawnForPlayer(playerId, lat, lng, env.NEARBY_RADIUS_METERS);
    }

    let rows = await listNearby(lat, lng, env.NEARBY_RADIUS_METERS);
    if (rows.length === 0 && env.AUTO_SPAWN_NEARBY) {
      if (!Number.isNaN(playerId) && playerId > 0) {
        await ensureSpawnForPlayer(playerId, lat, lng, env.NEARBY_RADIUS_METERS);
      } else {
        await seedNearby(lat, lng);
      }
      rows = await listNearby(lat, lng, env.NEARBY_RADIUS_METERS);
    }

    return res.json(rows);
  } catch (error) {
    console.error("Failed to list thiltapes", error);
    return res.sendStatus(500);
  }
});

router.post("/capturar", async (req, res) => {
  const playerId = Number(req.body?.playerId);
  const thiltapeId = Number(req.body?.thiltapeId);
  const lat = req.body?.lat;
  const lng = req.body?.lng;

  if (!Number.isInteger(playerId) || playerId <= 0 || !Number.isInteger(thiltapeId) || thiltapeId <= 0) {
    return res.sendStatus(400);
  }

  try {
    const playerExists = await existsPlayer(playerId);
    if (!playerExists) {
      return res.status(404).json({ erro: "Player nao encontrado" });
    }

    const exists = await existsThiltape(thiltapeId);
    if (!exists) {
      return res.sendStatus(404);
    }

    const hasCoords = lat !== undefined && lng !== undefined && !Number.isNaN(Number(lat)) && !Number.isNaN(Number(lng));
    if (hasCoords) {
      const allowed = await inCaptureRange(thiltapeId, Number(lat), Number(lng));
      if (!allowed) {
        return res.status(403).json({ erro: "Thiltape fora do raio de captura (100m)" });
      }
    }

    const captured = await capture(playerId, thiltapeId);
    return res.json({ capturado: captured });
  } catch (error) {
    console.error("Failed to capture thiltape", error);
    return res.sendStatus(500);
  }
});

router.get("/pokedex", async (req, res) => {
  const playerId = Number(req.query.playerId);
  if (!Number.isInteger(playerId) || playerId <= 0) {
    return res.sendStatus(400);
  }

  try {
    const rows = await listPokedex(playerId);
    return res.json(rows);
  } catch (error) {
    console.error("Failed to list pokedex", error);
    return res.sendStatus(500);
  }
});

router.get("/status-partida", async (req, res) => {
  const playerId = Number(req.query.playerId);
  if (!Number.isInteger(playerId) || playerId <= 0) {
    return res.status(400).json({ erro: "Parametro playerId eh obrigatorio" });
  }

  try {
    const totalThiltapes = await countAll();
    const totalCapturados = await countCaptured(playerId);
    return res.json({
      playerId,
      totalThiltapes,
      totalCapturados,
      finalizada: totalThiltapes > 0 && totalCapturados >= totalThiltapes
    });
  } catch (error) {
    console.error("Failed to get game status", error);
    return res.sendStatus(500);
  }
});

module.exports = router;
