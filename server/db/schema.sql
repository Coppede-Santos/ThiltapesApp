CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS thiltape (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    raridade VARCHAR(20) NOT NULL CHECK (raridade IN ('Comum', 'Raro', 'Lendario')),
    foto TEXT,
    localizacao GEOGRAPHY(POINT, 4326) NOT NULL
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

CREATE INDEX IF NOT EXISTS idx_thiltape_localizacao ON thiltape USING GIST (localizacao);
CREATE INDEX IF NOT EXISTS idx_captura_player ON captura (player_id);

-- Busca por distancia (100m)
-- :lat e :lng sao parametros da aplicacao
-- SELECT *
-- FROM thiltape
-- WHERE ST_Distance(
--   localizacao,
--   ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
-- ) <= 100;
