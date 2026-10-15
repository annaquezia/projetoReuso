#!/usr/bin/env bash
# Uso: bash scripts/iniciar.sh [--seed|--demo]
set -euo pipefail
raiz=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
jar="$raiz/projeto-reuso.jar"
[ -f "$jar" ] || jar="$raiz/target/projeto-reuso-0.3.0.jar"
if [ ! -f "$jar" ]; then
    echo 'JAR não encontrado. Gere a entrega ou execute ./mvnw package.' >&2
    exit 1
fi
if [ -n "${JAVA_HOME:-}" ]; then java="$JAVA_HOME/bin/java"; else java=java; fi
if ! "$java" -version >/dev/null 2>&1; then
    echo 'Instale Java 17 e configure JAVA_HOME ou o PATH.' >&2
    exit 1
fi
opcoes=(--server.address=127.0.0.1)
case "${1:-}" in
    --demo) opcoes+=(--spring.profiles.active=demo) ;;
    --seed) opcoes+=(--spring.profiles.active=seed --app.demo.seed=true) ;;
    '') ;;
    *) echo 'Uso: bash scripts/iniciar.sh [--seed|--demo]' >&2; exit 2 ;;
esac
if [ "${1:-}" != --demo ]; then
    export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/reuso_avaliacao?createDatabaseIfNotExist=true&serverTimezone=UTC}"
    if [ -z "${DB_USERNAME:-}" ]; then
        read -r -p 'Usuário do MySQL [root]: ' DB_USERNAME
        export DB_USERNAME="${DB_USERNAME:-root}"
    fi
    if [ "${DB_PASSWORD+x}" != x ]; then
        read -r -s -p 'Senha do MySQL (não será salva): ' DB_PASSWORD
        echo
        export DB_PASSWORD
    fi
fi
printf 'Abra http://127.0.0.1:%s depois da mensagem de inicialização. Ctrl+C encerra.\n' "${PORT:-8080}"
exec "$java" -jar "$jar" "${opcoes[@]}"
