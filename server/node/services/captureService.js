const { query, isPostgisEnabled } = require("../db/client");

async function existsThiltape(thiltapeId) {
  const result = await query("SELECT id FROM thiltape WHERE id = $1", [thiltapeId]);
  return result.rowCount > 0;
}

async function existsPlayer(playerId) {
  const result = await query("SELECT id FROM player WHERE id = $1", [playerId]);
  return result.rowCount > 0;
}

async function inCaptureRange(thiltapeId, lat, lng) {
  const result = await query(captureRangeSql(), [thiltapeId, lat, lng]);
  return result.rowCount > 0;
}

async function capture(playerId, thiltapeId) {
  const result = await query(
    "INSERT INTO captura (player_id, thiltape_id) VALUES ($1, $2) ON CONFLICT (player_id, thiltape_id) DO NOTHING",
    [playerId, thiltapeId]
  );
  return result.rowCount > 0;
}

async function listPokedex(playerId) {
  const result = await query(pokedexSql(), [playerId]);
  return result.rows;
}

async function countCaptured(playerId) {
  const result = await query("SELECT COUNT(*)::int AS count FROM captura WHERE player_id = $1", [playerId]);
  return result.rows[0].count;
}

function captureRangeSql() {
  if (isPostgisEnabled()) {
    return "SELECT id FROM thiltape WHERE id = $1 AND ST_Distance(localizacao, ST_SetSRID(ST_MakePoint($3, $2), 4326)::geography) <= 100";
  }

  return "SELECT id FROM thiltape WHERE id = $1 AND (6371000 * 2 * ASIN(SQRT(POWER(SIN(RADIANS(($2 - lat) / 2)), 2) + COS(RADIANS(lat)) * COS(RADIANS($2)) * POWER(SIN(RADIANS(($3 - lng) / 2)), 2)))) <= 100";
}

function pokedexSql() {
  if (isPostgisEnabled()) {
    return "SELECT t.id, t.nome, t.raridade, t.foto, ST_Y(t.localizacao::geometry) AS lat, ST_X(t.localizacao::geometry) AS lng FROM captura c JOIN thiltape t ON t.id = c.thiltape_id WHERE c.player_id = $1 ORDER BY c.data DESC";
  }

  return "SELECT t.id, t.nome, t.raridade, t.foto, t.lat, t.lng FROM captura c JOIN thiltape t ON t.id = c.thiltape_id WHERE c.player_id = $1 ORDER BY c.data DESC";
}

module.exports = {
  existsPlayer,
  existsThiltape,
  inCaptureRange,
  capture,
  listPokedex,
  countCaptured
};
