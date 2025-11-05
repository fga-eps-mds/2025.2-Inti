@echo off
setlocal

:: Define o modo. Pega o primeiro argumento (%~1 remove aspas). 
:: Se nao houver, usa 'emulator' como padrao.
set "MODE=%~1"
if "%MODE%"=="" set "MODE=emulator"

:: Passo 1: Verificar se ANDROID_HOME está configurado
if not defined ANDROID_HOME (
    echo Erro: A variavel de ambiente ANDROID_HOME nao esta definida.
    echo Por favor, configure-a para apontar para o diretorio do seu Android SDK.
    exit /b 1
)

:: ==================================================================
:: PASSO 2, 3, 4: LÓGICA CONDICIONAL PARA EMULADOR OU DISPOSITIVO
:: ==================================================================
if /I "%MODE%"=="emulator" (
    :: --- Bloco de código para o Emulador ---
    echo [INFO] Modo Emulador selecionado.

    echo [INFO] Procurando por emuladores (AVDs)...
    for /f %%i in ('%ANDROID_HOME%\emulator\emulator -list-avds') do (
        set "AVD_NAME=%%i"
        goto :found_avd
    )

    echo Erro: Nenhum emulador (AVD) encontrado.
    echo Por favor, crie um usando o AVD Manager.
    exit /b 1

    :found_avd
    echo [INFO] Usando o emulador: %AVD_NAME%

    :: Passo 3: Iniciar o emulador em uma nova janela (em segundo plano)
    echo [INFO] Iniciando o emulador...
    start "Emulator" "%ANDROID_HOME%\emulator\emulator.exe" -avd %AVD_NAME%

    :: Passo 4: Esperar o emulador carregar completamente
    echo [INFO] Aguardando o emulador inicializar por completo... (Isso pode levar alguns minutos)
    :wait_loop
    %ANDROID_HOME%\platform-tools\adb.exe wait-for-device
    %ANDROID_HOME%\platform-tools\adb.exe shell getprop sys.boot_completed | findstr "1" >nul
    if %errorlevel% neq 0 (
        echo [INFO] Ainda aguardando...
        timeout /t 3 >nul
        goto wait_loop
    )
    echo [INFO] Emulador pronto!

) else if /I "%MODE%"=="device" (
    :: --- Bloco de código para o Dispositivo Físico ---
    echo [INFO] Modo Dispositivo Fisico selecionado.
    echo [INFO] Aguardando dispositivo fisico via USB... (Certifique-se de que a depuracao USB esta ativada e autorizada)
    %ANDROID_HOME%\platform-tools\adb.exe wait-for-device
    echo [INFO] Dispositivo fisico detectado!

) else (
    :: --- Bloco de Erro para opção inválida ---
    echo Erro: Modo invalido '%MODE%'. Use 'emulator' ou 'device'.
    exit /b 1
)

:: ==================================================================
:: PASSOS SUBSEQUENTES (COMUNS A AMBOS OS MODOS)
:: ==================================================================

:: Passo 5: Iniciar o container Docker com o Metro Bundler
echo [INFO] Iniciando o container Docker com docker-compose...
docker-compose up -d --build

:: Passo 6: Configurar o redirecionamento de porta
echo [INFO] Configurando 'adb reverse' para a porta 8081...
%ANDROID_HOME%\platform-tools\adb.exe reverse tcp:8081 tcp:8081

:: Passo 7: Instalar e iniciar o aplicativo
echo [INFO] Instalando e iniciando o app no dispositivo/emulador...
npx react-native run-android

echo [INFO] Setup concluido! O app deve estar rodando.
endlocal
