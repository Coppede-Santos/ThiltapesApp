function normalizeJdbcUrl(value) {
  return value.startsWith("jdbc:") ? value.slice(5) : value;
}

function buildPgConfig(dbUrl, fallbackUser, fallbackPassword) {
  const parsed = new URL(normalizeJdbcUrl(dbUrl));
  const username = parsed.username || fallbackUser;
  const password = parsed.password || fallbackPassword;

  return {
    host: parsed.hostname,
    port: Number(parsed.port || 5432),
    database: parsed.pathname.replace(/^\//, ""),
    user: String(username),
    password: String(password)
  };
}

function quoteIdent(identifier) {
  return '"' + String(identifier).replace(/"/g, '""') + '"';
}

module.exports = {
  normalizeJdbcUrl,
  buildPgConfig,
  quoteIdent
};
