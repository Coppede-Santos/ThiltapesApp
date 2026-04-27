#!/bin/bash

echo "🚀 Iniciando configuração do Caçando Thiltapes na VM..."

# Verificar se Docker está instalado
if ! [ -x "$(command -v docker)" ]; then
  echo "❌ Erro: Docker não está instalado. Por favor, instale o Docker antes de continuar."
  exit 1
fi

# Ir para a pasta do servidor
cd server

# Subir os containers
echo "📦 Subindo banco de dados e backend..."
docker compose up -d --build

echo "✅ Tudo pronto! O servidor está rodando na porta 8080."
echo "🔗 Healthcheck: http://localhost:8080/thiltapes-api/health"
echo "🛠️ Admin Dashboard: http://localhost:8080/thiltapes-api/admin/dashboard"
