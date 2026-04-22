# Cacando Thiltapes

Projeto de jogo mobile com captura por geolocalizacao.

Stack atual:
1. App Android em Java
2. Backend Node.js + Express
3. PostgreSQL (com fallback sem PostGIS)

## Como rodar o servidor (CMD)

Na raiz do projeto, execute:

```bat
npm --prefix server install
npm --prefix server start
```

Teste rapido de healthcheck:

```bat
curl http://localhost:8080/thiltapes-api/health
```

Se retornar `{"ok":true}`, o backend esta online.

Alternativa equivalente:

```bat
cd server
npm install
npm start
```

## URLs importantes

1. API health: `http://localhost:8080/thiltapes-api/health`
2. Admin login: `http://localhost:8080/thiltapes-api/admin/login`
3. Admin dashboard: `http://localhost:8080/thiltapes-api/admin/dashboard`

Credenciais admin padrao:
1. Usuario: `admin`
2. Senha: `admin123`

## Como funciona o jogo

1. Jogador entra no app e cria/continua sessao.
2. App envia `playerId + lat + lng` para buscar thiltapes proximos.
3. O backend ancora a partida na localizacao do jogador e gera thiltapes ao redor quando necessario.
4. No mapa, o jogador toca no marcador e clica em Capturar (ate 100m).
5. Capturas vao para a Pokedex.

## Arquitetura do backend Node

```text
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
			adminRoutes.js      # endpoints admin e dashboard
		services/
			playerService.js
			thiltapeService.js
			captureService.js
		utils/
			geo.js
```

## Endpoints principais

1. `POST /thiltapes-api/player`
2. `GET /thiltapes-api/thiltapes?playerId={id}&lat={lat}&lng={lng}`
3. `POST /thiltapes-api/capturar`
4. `GET /thiltapes-api/pokedex?playerId={id}`
5. `GET /thiltapes-api/status-partida?playerId={id}`

## Admin dashboard

No painel admin voce consegue:
1. Listar usuarios e thiltapes
2. Adicionar, editar e excluir usuarios
3. Adicionar, editar e excluir thiltapes

## Android (emulador)

No emulador, `localhost` da maquina host deve ser acessado via `10.0.2.2`.

Arquivo de configuracao da API no app:
1. `app/src/main/java/com/example/thiltapeshunting/network/ApiConfig.java`

## Google Maps API Key

Configure sem hardcode:
1. Variavel de ambiente `GOOGLE_MAPS_API_KEY`
2. Ou `local.properties` com `GOOGLE_MAPS_API_KEY=SUA_CHAVE`

Tambem e aceito `NEXT_PUBLIC_GOOGLE_MAPS_KEY`.
