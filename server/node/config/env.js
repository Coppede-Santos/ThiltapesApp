const path = require("path");

const PORT = Number(process.env.PORT || 8080);
const DB_URL = process.env.DB_URL || "jdbc:postgresql://localhost:5432/thiltapes";
const DB_USER = process.env.DB_USER || "postgres";
const DB_PASSWORD = process.env.DB_PASSWORD ?? "postgres";
const DB_AUTO_INIT = process.env.DB_AUTO_INIT !== "false";

const ADMIN_USER = process.env.ADMIN_USER || "admin";
const ADMIN_PASS = process.env.ADMIN_PASS || "admin123";
const SESSION_SECRET = process.env.SESSION_SECRET || "thiltapes-dev-secret";

const AUTO_SPAWN_NEARBY = process.env.THILTAPES_AUTO_SPAWN_NEARBY !== "false";
const NEARBY_RADIUS_METERS = Number(process.env.THILTAPES_NEARBY_RADIUS_METERS || 1000);

const ROOT_DIR = path.resolve(__dirname, "..", "..");
const UPLOADS_DIR = path.join(ROOT_DIR, "uploads");
const VIEWS_DIR = path.join(ROOT_DIR, "views");
const DB_SCHEMA_FILE = path.join(ROOT_DIR, "db", "schema.sql");

module.exports = {
  PORT,
  DB_URL,
  DB_USER,
  DB_PASSWORD,
  DB_AUTO_INIT,
  ADMIN_USER,
  ADMIN_PASS,
  SESSION_SECRET,
  AUTO_SPAWN_NEARBY,
  NEARBY_RADIUS_METERS,
  ROOT_DIR,
  UPLOADS_DIR,
  VIEWS_DIR,
  DB_SCHEMA_FILE
};
