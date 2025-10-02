#!/bin/bash

# Define cores para os logs
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Função para logar mensagens
log() {
  echo -e "\n${GREEN}[INFO]${NC} $1"
}

# ==================================================================
# PASSO ZERO: Derrubar containers antigos para garantir um ambiente limpo
# ==================================================================
log "Garantindo que containers antigos estao parados e removidos..."
docker-compose down
log "Ambiente Docker limpo."

# Passo 1: Verificar se o ANDROID_HOME está configurado
if [ -z "$ANDROID_HOME" ]; then
  echo "Erro: A variável de ambiente ANDROID_HOME não está definida."
  exit 1
fi

# Adiciona as ferramentas do emulador e platform-tools ao PATH para esta sessão
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools

# Passo 2: Encontrar o primeiro emulador disponível
log "Procurando por emuladores (AVDs)..."
AVD_NAME=$(emulator -list-avds | head -n 1)

if [ -z "$AVD_NAME" ]; then
  echo "Erro: Nenhum emulador (AVD) encontrado."
  exit 1
fi
log "Usando o emulador: $AVD_NAME"

# Passo 3: Iniciar o emulador em segundo plano
log "Iniciando o emulador..."
nohup emulator -avd "$AVD_NAME" > /dev/null 2>&1 &

# Passo 4: Esperar o emulador carregar completamente
log "Aguardando o emulador inicializar por completo..."
adb wait-for-device

# Espera até que a propriedade 'sys.boot_completed' seja '1', removendo caracteres extras
until [[ "$(adb shell getprop sys.boot_completed | tr -d '\r\n')" == "1" ]]; do
  echo -n "."
  sleep 2
done
log "Emulador pronto!"

# Passo 5: Iniciar o container Docker com o Metro Bundler
log "Iniciando o container Docker com docker-compose..."
docker-compose up -d --build

# ==================================================================
# PASSO 5.5: Aguardar o Metro Bundler do Docker ficar pronto
# ==================================================================
log "Aguardando o servidor Metro na porta 8081 ficar pronto..."
until curl --output /dev/null --silent --head --fail http://localhost:8081; do
  echo -n "."
  sleep 2
done
log "Servidor Metro detectado! Prosseguindo..."

# Passo 6: Configurar o redirecionamento de porta
log "Configurando 'adb reverse' para a porta 8081..."
adb reverse tcp:8081 tcp:8081

# Passo 7: Instalar e iniciar o aplicativo
log "Instalando e iniciando o app no emulador..."
npx react-native run-android --port 8081

log "Setup concluído! O app deve estar rodando no emulador."
