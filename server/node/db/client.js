const fs = require("fs");
const { Pool } = require("pg");

const env = require("../config/env");
const { buildPgConfig, quoteIdent } = require("../config/postgres");
const { fallbackSchemaSql, withSpawnAnchorSchema } = require("./schema");

const pgConfig = buildPgConfig(env.DB_URL, env.DB_USER, env.DB_PASSWORD);

let pool = null;
let usesPostgis = true;

async function initDb() {
  if (env.DB_AUTO_INIT) {
    await ensureDatabaseAndSchema(pgConfig);
  }

  pool = new Pool(pgConfig);
}

async function query(sql, params) {
  if (!pool) {
    throw new Error("Database pool is not initialized");
  }
  return pool.query(sql, params);
}

function isPostgisEnabled() {
  return usesPostgis;
}

async function ensureDatabaseAndSchema(config) {
  const adminPool = new Pool({
    host: config.host,
    port: config.port,
    database: "postgres",
    user: config.user,
    password: config.password
  });

  try {
    const dbCheck = await adminPool.query("SELECT 1 FROM pg_database WHERE datname = $1", [config.database]);
    if (dbCheck.rowCount === 0) {
      await adminPool.query(`CREATE DATABASE ${quoteIdent(config.database)}`);
      console.log(`Database ${config.database} created`);
    }
  } finally {
    await adminPool.end();
  }

  const appPool = new Pool(config);
  try {
    let schemaSql;
    try {
      await appPool.query("CREATE EXTENSION IF NOT EXISTS postgis");
      usesPostgis = true;
      const raw = fs.readFileSync(env.DB_SCHEMA_FILE, "utf8");
      schemaSql = withSpawnAnchorSchema(raw.replace(/CREATE EXTENSION IF NOT EXISTS postgis;\s*/i, ""));
    } catch (_error) {
      usesPostgis = false;
      console.warn("PostGIS unavailable, using fallback geometry mode");
      schemaSql = fallbackSchemaSql();
    }

    await appPool.query(schemaSql);
    console.log("Database schema initialized");
  } finally {
    await appPool.end();
  }
}

module.exports = {
  initDb,
  query,
  isPostgisEnabled
};
