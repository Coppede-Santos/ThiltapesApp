const express = require("express");
const fs = require("fs");
const session = require("express-session");

const env = require("./config/env");
const apiRoutes = require("./routes/apiRoutes");
const adminRoutes = require("./routes/adminRoutes");

function createApp() {
  fs.mkdirSync(env.UPLOADS_DIR, { recursive: true });

  const app = express();
  app.set("view engine", "ejs");
  app.set("views", env.VIEWS_DIR);

  app.use(express.json());
  app.use(express.urlencoded({ extended: true }));
  app.use(
    session({
      secret: env.SESSION_SECRET,
      resave: false,
      saveUninitialized: false,
      cookie: {
        httpOnly: true,
        maxAge: 1000 * 60 * 60 * 4
      }
    })
  );
  app.use("/uploads", express.static(env.UPLOADS_DIR));

  app.use("/thiltapes-api", apiRoutes);
  app.use("/thiltapes-api", adminRoutes);
  app.use("/", apiRoutes);
  app.use("/", adminRoutes);

  return app;
}

module.exports = {
  createApp
};
