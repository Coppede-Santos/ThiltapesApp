const { query } = require("../db/client");

async function createPlayer(nome) {
  const result = await query("INSERT INTO player (nome) VALUES ($1) RETURNING id, nome", [nome]);
  return result.rows[0];
}

async function listPlayers() {
  const result = await query(
    "SELECT p.id, p.nome, COUNT(c.id)::int AS capturas FROM player p LEFT JOIN captura c ON c.player_id = p.id GROUP BY p.id, p.nome ORDER BY p.id DESC",
    []
  );
  return result.rows;
}

async function updatePlayer(id, nome) {
  const result = await query("UPDATE player SET nome = $2 WHERE id = $1 RETURNING id, nome", [id, nome]);
  return result.rows[0] || null;
}

async function deletePlayer(id) {
  const result = await query("DELETE FROM player WHERE id = $1", [id]);
  return result.rowCount > 0;
}

module.exports = {
  createPlayer,
  listPlayers,
  updatePlayer,
  deletePlayer
};
