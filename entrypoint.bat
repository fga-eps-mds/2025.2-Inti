@echo off
setlocal

:: Define o modo de execução. Pega o primeiro argumento (%1). Se não houver, usa 'emulator' como padrão.
set "MODE=%~1"
if "%MODE%"=="" set "MODE=emulator"

echo [INFO] Modo selecionado: %MODE%

:: Passo 1: Verificar se ANDROID_HOME está configurado
if not defined ANDROID_HOME (
    echo Erro: A variavel de ambiente ANDROID_HOME nao esta definida.
    echo Por favor, configure-a para apontar para o diretorio do seu Android SDK.
    exit /b 1
)

:: Passo 2: Derrubar containers antigos
echo [INFO] Garantindo que containers antigos estao parados e removidos...
docker-compose down
echo [INFO] Ambiente Docker limpo.

:: Passo 3: Iniciar o container Docker com o Metro Bundler PRIMEIRO
echo [INFO] Iniciando o container Docker com docker-compose...
docker-compose up -d

:: Passo 4: Aguardar a PORTA 8081 ficar pronta
echo [INFO] Aguardando a porta 8081 ficar pronta...
:wait_port
timeout /t 2 /nobreak >nul
netstat -an | findstr ":8081" | findstr "LISTENING" >nul
if %errorlevel% neq 0 (
    echo Aguardando porta 8081...
    goto wait_port
)
echo [INFO] Porta 8081 detectada! Prosseguindo...

:: ==================================================================
:: Passo 5, 6, 7: LÓGICA CONDICIONAL PARA EMULADOR OU DISPOSITIVO
:: ==================================================================
if /i "%MODE%"=="emulator" (
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

    echo [INFO] Iniciando o emulador...
    start "Emulator" "%ANDROID_HOME%\emulator\emulator.exe" -avd %AVD_NAME%

    echo [INFO] Aguardando o emulador inicializar por completo...
:wait_emulator
    %ANDROID_HOME%\platform-tools\adb.exe wait-for-device
    %ANDROID_HOME%\platform-tools\adb.exe shell getprop sys.boot_completed 2>nul | findstr "1" >nul
    if %errorlevel% neq 0 (
        echo [INFO] Ainda aguardando emulador...
        timeout /t 2 /nobreak >nul
        goto wait_emulator
    )
    echo [INFO] Emulador pronto!

) else if /i "%MODE%"=="device" (
    :: --- Bloco de código para o Dispositivo Físico ---
    echo [INFO] Modo Dispositivo Fisico selecionado.
    echo [INFO] Aguardando dispositivo fisico via USB...
    echo [INFO] Certifique-se de que a depuracao USB esta ativada e autorizada.
    %ANDROID_HOME%\platform-tools\adb.exe wait-for-device
    echo [INFO] Dispositivo fisico detectado!

) else (
    :: --- Bloco de Erro para opção inválida ---
    echo Erro: Modo invalido '%MODE%'. Use 'emulator' ou 'device'.
    exit /b 1
)

:: Passo 8: Configurar o redirecionamento de porta
echo [INFO] Configurando 'adb reverse' para a porta 8081...
%ANDROID_HOME%\platform-tools\adb.exe reverse tcp:8081 tcp:8081

:: Passo 9: Build e Instalação via Gradle
echo [INFO] Compilando o app com Gradle (assembleDebug)...
cd android
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo Erro ao compilar o app.
    cd ..
    exit /b 1
)

echo [INFO] Instalando o app no dispositivo (installDebug)...
call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo Erro ao instalar o app.
    cd ..
    exit /b 1
)
cd ..

:: Passo 10: Iniciar o aplicativo
set "PACKAGE_NAME=com.musa"
echo [INFO] Iniciando o app (%PACKAGE_NAME%) no dispositivo...
%ANDROID_HOME%\platform-tools\adb.exe shell am start -n "%PACKAGE_NAME%/%PACKAGE_NAME%.MainActivity"

echo [INFO] Setup concluido! O app deve estar iniciando no dispositivo.
endlocal