# Thiltapes API (Node.js + Express + PostGIS)

Backend migrado para Node.js, mantendo compatibilidade de contratos com o app Android.

## Requisitos
- Node.js 18+
- PostgreSQL
- PostGIS e opcional (se nao existir, o backend usa modo fallback com lat/lng numericos)

## Banco de dados
1. Crie o banco `thiltapes`.
2. Execute `server/db/schema.sql`.
3. Configure variaveis de ambiente:
   - `DB_URL` (default: `jdbc:postgresql://localhost:5432/thiltapes` ou `postgresql://localhost:5432/thiltapes`)
   - `DB_USER` (default: `postgres`)
   - `DB_PASSWORD` (default: `postgres`)

## Admin
- Login: `/thiltapes-api/admin/login`
- Credenciais por ambiente:
  - `ADMIN_USER` (default: `admin`)
  - `ADMIN_PASS` (default: `admin123`)
- Dashboard: `/thiltapes-api/admin/dashboard`

## Endpoints (compatibilidade)
- `POST /thiltapes-api/player`
- `GET /thiltapes-api/thiltapes?playerId={id}&lat={lat}&lng={lng}`
- `POST /thiltapes-api/capturar`
- `GET /thiltapes-api/pokedex?playerId={id}`
- `GET /thiltapes-api/status-partida?playerId={id}`
- `POST /thiltapes-api/admin/thiltape` (multipart)

Tambem estao disponiveis sem prefixo (`/player`, `/thiltapes`, etc.) para uso local.

## Executar
```bash
cd server
npm install
npm start
```

Servidor padrao: `http://localhost:8080`.

## Auto setup do banco
- Por padrao, o backend tenta criar o banco e aplicar `server/db/schema.sql` automaticamente no startup.
- Para desativar: defina `DB_AUTO_INIT=false`.

## Spawn de teste no mapa
- Quando o app chama `/thiltapes?playerId=...&lat=...&lng=...`, o backend ancora o jogo do player nessa localizacao e gera thiltapes por perto no inicio da partida.
- Se nao houver nenhum no raio, ele cria automaticamente alguns thiltapes ao redor da posicao para facilitar testes no emulador.
- Configuracoes:
  - `THILTAPES_AUTO_SPAWN_NEARBY=true|false` (default: `true`)
  - `THILTAPES_NEARBY_RADIUS_METERS=1000` (default: `1000`)

## Estrutura Node
```
server/
  index.js                # bootstrap do servidor
  node/
    app.js                # setup do Express e middlewares
    config/
      env.js              # variaveis de ambiente
      postgres.js         # parse/config de conexao
    db/
      client.js           # pool, init e schema bootstrap
      schema.js           # fallback sem PostGIS
    routes/
      apiRoutes.js        # endpoints do app
      adminRoutes.js      # endpoints admin
    services/
      playerService.js
      thiltapeService.js
      captureService.js
    utils/
      geo.js
```
