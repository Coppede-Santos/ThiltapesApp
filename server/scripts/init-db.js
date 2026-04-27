const fs = require("fs");
const path = require("path");
const { Pool } = require("pg");

const baseUrl = (process.env.DB_URL || "jdbc:postgresql://localhost:5432/thiltapes").replace(/^jdbc:/, "");
const parsed = new URL(baseUrl);
const targetDb = parsed.pathname.replace(/^\//, "") || "thiltapes";
const user = String(process.env.DB_USER || "postgres");
const password = String(process.env.DB_PASSWORD ?? "postgres");

async function main() {
  const adminPool = new Pool({
    host: parsed.hostname,
    port: Number(parsed.port || 5432),
    database: "postgres",
    user,
    password
  });

  try {
    await adminPool.query(`CREATE DATABASE ${quoteIdent(targetDb)}`);
    console.log(`Database ${targetDb} created`);
  } catch (error) {
    // 42P04 = duplicate_database
    if (error.code === "42P04") {
      console.log(`Database ${targetDb} already exists`);
    } else {
      throw error;
    }
  } finally {
    await adminPool.end();
  }

  const appPool = new Pool({
    host: parsed.hostname,
    port: Number(parsed.port || 5432),
    database: targetDb,
    user,
    password
  });

  try {
    const schemaPath = path.join(__dirname, "..", "db", "schema.sql");
    const schemaSql = fs.readFileSync(schemaPath, "utf8");
    await appPool.query(schemaSql);
    console.log("Schema applied successfully");
  } finally {
    await appPool.end();
  }
}

function quoteIdent(name) {
  return '"' + String(name).replace(/"/g, '""') + '"';
}

main().catch((error) => {
  console.error("Failed to initialize DB:", error.message);
  process.exit(1);
});
