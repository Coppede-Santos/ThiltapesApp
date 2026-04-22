function fallbackSchemaSql() {
  return `
CREATE TABLE IF NOT EXISTS thiltape (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    raridade VARCHAR(20) NOT NULL CHECK (raridade IN ('Comum', 'Raro', 'Lendario')),
    foto TEXT,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS player (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS captura (
    id SERIAL PRIMARY KEY,
    player_id INT NOT NULL REFERENCES player(id) ON DELETE CASCADE,
    thiltape_id INT NOT NULL REFERENCES thiltape(id) ON DELETE CASCADE,
    data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (player_id, thiltape_id)
);

CREATE TABLE IF NOT EXISTS player_spawn_anchor (
    player_id INT PRIMARY KEY REFERENCES player(id) ON DELETE CASCADE,
    anchor_lat DOUBLE PRECISION NOT NULL,
    anchor_lng DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_captura_player ON captura (player_id);
CREATE INDEX IF NOT EXISTS idx_thiltape_lat ON thiltape (lat);
CREATE INDEX IF NOT EXISTS idx_thiltape_lng ON thiltape (lng);
`;
}

function withSpawnAnchorSchema(baseSql) {
  return `${baseSql}\n\nCREATE TABLE IF NOT EXISTS player_spawn_anchor (\n    player_id INT PRIMARY KEY REFERENCES player(id) ON DELETE CASCADE,\n    anchor_lat DOUBLE PRECISION NOT NULL,\n    anchor_lng DOUBLE PRECISION NOT NULL,\n    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP\n);\n`;
}

module.exports = {
  fallbackSchemaSql,
  withSpawnAnchorSchema
};
