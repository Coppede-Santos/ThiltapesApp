const { query, isPostgisEnabled } = require("../db/client");
const { offsetCoordinates } = require("../utils/geo");

async function listAll() {
  const result = await query(listSql(), []);
  return result.rows;
}

async function getById(id) {
  const result = await query(byIdSql(), [id]);
  return result.rows[0] || null;
}

async function listNearby(lat, lng, radiusMeters) {
  const result = await query(nearbySql(), [lat, lng, radiusMeters]);
  return result.rows;
}

async function ensureSpawnForPlayer(playerId, lat, lng, radiusMeters) {
  const playerExists = await query("SELECT id FROM player WHERE id = $1", [playerId]);
  if (playerExists.rowCount === 0) {
    await seedNearby(lat, lng);
    return;
  }

  const existingAnchor = await query("SELECT player_id FROM player_spawn_anchor WHERE player_id = $1", [playerId]);
  if (existingAnchor.rowCount === 0) {
    await seedNearby(lat, lng);
    await query(
      "INSERT INTO player_spawn_anchor (player_id, anchor_lat, anchor_lng) VALUES ($1, $2, $3)",
      [playerId, lat, lng]
    );
  }

  const nearby = await listNearby(lat, lng, radiusMeters);
  if (nearby.length === 0) {
    await seedNearby(lat, lng);
  }
}

async function createOne(nome, raridade, foto, lat, lng) {
  const result = await query(createSql(), [nome, raridade, foto, lat, lng]);
  return result.rows[0];
}

async function updateOne(id, nome, raridade, foto, lat, lng) {
  const result = await query(updateSql(), [id, nome, raridade, foto, lat, lng]);
  return result.rows[0] || null;
}

async function deleteOne(id) {
  const result = await query("DELETE FROM thiltape WHERE id = $1", [id]);
  return result.rowCount > 0;
}

async function countAll() {
  const result = await query("SELECT COUNT(*)::int AS count FROM thiltape", []);
  return result.rows[0].count;
}

async function seedNearby(centerLat, centerLng) {
  const items = [
    { nome: "Thiltape Brisa", raridade: "Comum", dx: 40, dy: 20 },
    { nome: "Thiltape Pedra", raridade: "Comum", dx: -55, dy: 35 },
    { nome: "Thiltape Nimbus", raridade: "Raro", dx: 70, dy: -30 },
    { nome: "Thiltape Lunar", raridade: "Raro", dx: -80, dy: -45 },
    { nome: "Thiltape Aurora", raridade: "Lendario", dx: 25, dy: 90 }
  ];

  for (const item of items) {
    const next = offsetCoordinates(centerLat, centerLng, item.dx, item.dy);
    await createOne(item.nome, item.raridade, "", next.lat, next.lng);
  }
}

function listSql() {
  if (isPostgisEnabled()) {
    return "SELECT id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng FROM thiltape";
  }

  return "SELECT id, nome, raridade, foto, lat, lng FROM thiltape";
}

function nearbySql() {
  if (isPostgisEnabled()) {
    return "SELECT id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng, ST_Distance(localizacao, ST_SetSRID(ST_MakePoint($2, $1), 4326)::geography) AS distancia FROM thiltape WHERE ST_DWithin(localizacao, ST_SetSRID(ST_MakePoint($2, $1), 4326)::geography, $3) ORDER BY distancia ASC";
  }

  return "SELECT id, nome, raridade, foto, lat, lng, (6371000 * 2 * ASIN(SQRT(POWER(SIN(RADIANS(($1 - lat) / 2)), 2) + COS(RADIANS(lat)) * COS(RADIANS($1)) * POWER(SIN(RADIANS(($2 - lng) / 2)), 2)))) AS distancia FROM thiltape WHERE (6371000 * 2 * ASIN(SQRT(POWER(SIN(RADIANS(($1 - lat) / 2)), 2) + COS(RADIANS(lat)) * COS(RADIANS($1)) * POWER(SIN(RADIANS(($2 - lng) / 2)), 2)))) <= $3 ORDER BY distancia ASC";
}

function byIdSql() {
  if (isPostgisEnabled()) {
    return "SELECT id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng FROM thiltape WHERE id = $1";
  }

  return "SELECT id, nome, raridade, foto, lat, lng FROM thiltape WHERE id = $1";
}

function createSql() {
  if (isPostgisEnabled()) {
    return "INSERT INTO thiltape (nome, raridade, foto, localizacao) VALUES ($1, $2, $3, ST_SetSRID(ST_MakePoint($5, $4), 4326)::geography) RETURNING id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng";
  }

  return "INSERT INTO thiltape (nome, raridade, foto, lat, lng) VALUES ($1, $2, $3, $4, $5) RETURNING id, nome, raridade, foto, lat, lng";
}

function updateSql() {
  if (isPostgisEnabled()) {
    return "UPDATE thiltape SET nome = $2, raridade = $3, foto = $4, localizacao = ST_SetSRID(ST_MakePoint($6, $5), 4326)::geography WHERE id = $1 RETURNING id, nome, raridade, foto, ST_Y(localizacao::geometry) AS lat, ST_X(localizacao::geometry) AS lng";
  }

  return "UPDATE thiltape SET nome = $2, raridade = $3, foto = $4, lat = $5, lng = $6 WHERE id = $1 RETURNING id, nome, raridade, foto, lat, lng";
}

module.exports = {
  listAll,
  getById,
  listNearby,
  ensureSpawnForPlayer,
  seedNearby,
  createOne,
  updateOne,
  deleteOne,
  countAll
};
