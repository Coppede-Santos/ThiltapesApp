const env = require("./node/config/env");
const { createApp } = require("./node/app");
const { initDb } = require("./node/db/client");

start();

async function start() {
  try {
    await initDb();

    const app = createApp();
    app.listen(env.PORT, () => {
      console.log(`Thiltapes Node API running on port ${env.PORT}`);
    });
  } catch (error) {
    console.error("Failed to start server:", error.message);
    process.exit(1);
  }
}
