#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/home/univates/thiltapes/server"
WAR_PATH="$APP_DIR/target/thiltapes-api.war"
TOMCAT_WEBAPPS="/var/lib/tomcat10/webapps"
OVERRIDE_DIR="/etc/systemd/system/tomcat10.service.d"
OVERRIDE_FILE="$OVERRIDE_DIR/override.conf"

DB_URL="jdbc:postgresql://localhost:5432/thiltapes"
DB_USER="postgres"
DB_PASSWORD="postgres"
ADMIN_USER="admin"
ADMIN_PASS="admin123"

echo "[1/8] Installing dependencies"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y openjdk-21-jdk maven tomcat10 postgresql postgresql-contrib postgis postgresql-postgis curl

echo "[2/8] Enabling services"
systemctl enable --now postgresql
systemctl enable --now tomcat10

echo "[3/8] Preparing database"
db_exists="$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='thiltapes';" | tr -d '[:space:]')"
if [ "$db_exists" != "1" ]; then
	sudo -u postgres createdb thiltapes
fi
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD '${DB_PASSWORD}';"
sudo -u postgres psql -d thiltapes < "$APP_DIR/db/schema.sql"

echo "[4/8] Building WAR"
cd "$APP_DIR"
mvn -q clean package

echo "[5/8] Configuring Tomcat environment"
mkdir -p "$OVERRIDE_DIR"
cat > "$OVERRIDE_FILE" <<EOF
[Service]
Environment=DB_URL=${DB_URL}
Environment=DB_USER=${DB_USER}
Environment=DB_PASSWORD=${DB_PASSWORD}
Environment=ADMIN_USER=${ADMIN_USER}
Environment=ADMIN_PASS=${ADMIN_PASS}
EOF

echo "[6/8] Deploying WAR"
rm -rf "$TOMCAT_WEBAPPS/thiltapes-api" "$TOMCAT_WEBAPPS/thiltapes-api.war"
cp "$WAR_PATH" "$TOMCAT_WEBAPPS/thiltapes-api.war"
chown tomcat:tomcat "$TOMCAT_WEBAPPS/thiltapes-api.war"

echo "[7/8] Restarting Tomcat"
systemctl daemon-reload
systemctl restart tomcat10
sleep 8

echo "[8/8] Health check"
systemctl --no-pager --full status tomcat10 | sed -n '1,12p'
curl -sS http://localhost:8080/thiltapes-api/ | head -n 2 || true

echo "Provisioning completed successfully."
