#!/bin/bash
set -e
# Define cores para os logs
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Função para logar mensagens
log() {
  echo -e "\n${GREEN}[INFO]${NC} $1"
}

# PASSO 0: Derrubar containers antigos
log "Garantindo que containers antigos estao parados e removidos..."
docker-compose down
log "Ambiente Docker limpo."

# PASSO 1: Verificar se o ANDROID_HOME está configurado
if [ -z "$ANDROID_HOME" ]; then
  echo "Erro: A variável de ambiente ANDROID_HOME não está definida."
  exit 1
fi
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools

# PASSO 2: Iniciar o container Docker com o Metro Bundler PRIMEIRO
log "Iniciando o container Docker com docker-compose..."
docker-compose up -d --build

# PASSO 3: Aguardar a PORTA 8081 ficar pronta
log "Aguardando a porta 8081 ficar pronta..."
until /usr/bin/nc -zvw1 127.0.0.1 8081 &> /dev/null; do
  echo -n "."
  sleep 2
done
log "Porta 8081 detectada! Prosseguindo..."

# PASSO 4: Encontrar o emulador disponível
log "Procurando por emuladores (AVDs)..."
AVD_NAME=$(emulator -list-avds | head -n 1)
if [ -z "$AVD_NAME" ]; then
  echo "Erro: Nenhum emulador (AVD) encontrado."
  exit 1
fi
log "Usando o emulador: $AVD_NAME"

# PASSO 5: Iniciar o emulador em segundo plano AGORA
log "Iniciando o emulador..."
nohup emulator -avd "$AVD_NAME" > /dev/null 2>&1 &

# PASSO 6: Esperar o emulador carregar completamente
log "Aguardando o emulador inicializar por completo..."
adb wait-for-device
until [[ "$(adb shell getprop sys.boot_completed | tr -d '\r\n')" == "1" ]]; do
  echo -n "."
  sleep 2
done
log "Emulador pronto!"

# PASSO 7: Configurar o redirecionamento de porta
log "Configurando 'adb reverse' para a porta 8081..."
adb reverse tcp:8081 tcp:8081

# PASSO 8: Build e Instalação via Gradle (a forma confiável)
log "Compilando o app com Gradle (assembleDebug)..."
chmod +x android/gradlew
(cd android && ./gradlew assembleDebug)

log "Instalando o app no emulador (installDebug)..."
(cd android && ./gradlew installDebug)

# Confirme o nome do pacote em 'android/app/src/main/AndroidManifest.xml'
PACKAGE_NAME="com.auraapp" 

log "Iniciando o app ($PACKAGE_NAME) no emulador..."
adb shell am start -n "$PACKAGE_NAME/$PACKAGE_NAME.MainActivity"

log "Setup concluído! O app deve estar iniciando no emulador."
