# Uso: ./scripts/iniciar.ps1 [-Seed|-Demo]
param([switch]$Seed, [switch]$Demo)
$ErrorActionPreference = 'Stop'
if ($Seed -and $Demo) { throw 'Escolha -Seed ou -Demo.' }
$raiz = Split-Path $PSScriptRoot -Parent
$jar = Join-Path $raiz 'projeto-reuso.jar'
if (!(Test-Path $jar)) { $jar = Join-Path $raiz 'target/projeto-reuso-0.3.0.jar' }
if (!(Test-Path $jar)) { throw 'JAR não encontrado. Gere a entrega ou execute ./mvnw.cmd package.' }
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
if (!(Get-Command $java -ErrorAction SilentlyContinue)) { throw 'Instale Java 17 e configure JAVA_HOME ou o PATH.' }
$anteriores = @{}
foreach ($chave in 'DB_URL','DB_USERNAME','DB_PASSWORD') {
    $anteriores[$chave] = [Environment]::GetEnvironmentVariable($chave, 'Process')
}
try {
    $opcoes = @('--server.address=127.0.0.1')
    if ($Demo) { $opcoes += '--spring.profiles.active=demo' }
    else {
        if (!$env:DB_URL) { $env:DB_URL = 'jdbc:mysql://localhost:3306/reuso_avaliacao?createDatabaseIfNotExist=true&serverTimezone=UTC' }
        if (!$env:DB_USERNAME) {
            $usuario = Read-Host 'Usuário do MySQL [root]'
            $env:DB_USERNAME = if ($usuario) { $usuario } else { 'root' }
        }
        if ($null -eq [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Process')) {
            $segredo = Read-Host 'Senha do MySQL (não será salva)' -AsSecureString
            $ponteiro = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segredo)
            try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ponteiro) }
            finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ponteiro); $segredo.Dispose() }
        }
        if ($Seed) { $opcoes += @('--spring.profiles.active=seed','--app.demo.seed=true') }
    }
    $porta = if ($env:PORT) { $env:PORT } else { '8080' }
    Write-Host "Abra http://127.0.0.1:$porta depois da mensagem de inicialização. Ctrl+C encerra."
    & $java -jar $jar @opcoes
    $resultado = $LASTEXITCODE
}
finally {
    foreach ($chave in $anteriores.Keys) { [Environment]::SetEnvironmentVariable($chave, $anteriores[$chave], 'Process') }
}
exit $resultado
