#!/bin/bash

# Define cores para os logs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para logar mensagens
log() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

# Passo 1: Verificar se o ANDROID_HOME está configurado
if [ -z "$ANDROID_HOME" ]; then
  echo "Erro: A variável de ambiente ANDROID_HOME não está definida."
  echo "Por favor, configure-a para apontar para o diretório do seu Android SDK."
  exit 1
fi

# Adiciona as ferramentas do emulador ao PATH para esta sessão
export PATH=$PATH:$ANDROID_HOME/emulator

# Passo 2: Encontrar o primeiro emulador disponível
log "Procurando por emuladores (AVDs)..."
AVD_NAME=$(emulator -list-avds | head -n 1)

if [ -z "$AVD_NAME" ]; then
  echo "Erro: Nenhum emulador (AVD) encontrado."
  echo "Por favor, crie um usando o AVD Manager."
  exit 1
fi

log "Usando o emulador: $AVD_NAME"

# Passo 3: Iniciar o emulador em segundo plano
log "Iniciando o emulador..."
# 'nohup' e '&' garantem que o emulador continue rodando se o terminal for fechado
nohup emulator -avd "$AVD_NAME" > /dev/null 2>&1 &

# Passo 4: Esperar o emulador carregar completamente
log "Aguardando o emulador inicializar por completo... (Isso pode levar alguns minutos)"
adb wait-for-device

# Espera até que a propriedade 'sys.boot_completed' seja '1'
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null)" == "1" ]]; do
  sleep 2
done

log "Emulador pronto!"

# Passo 5: Iniciar o container Docker com o Metro Bundler
log "Iniciando o container Docker com docker-compose..."
docker-compose up -d --build

# Passo 6: Configurar o redirecionamento de porta
log "Configurando 'adb reverse' para a porta 8081..."
adb reverse tcp:8081 tcp:8081

# Passo 7: Instalar e iniciar o aplicativo
log "Instalando e iniciando o app no emulador..."
npx react-native run-android

log "Setup concluído! O app deve estar rodando no emulador."