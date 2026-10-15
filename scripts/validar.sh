#!/bin/sh
# Uso: sh scripts/validar.sh [--mysql]; MySQL requer DB_USERNAME/DB_PASSWORD.
set -eu
cd "$(dirname "$0")/.."
if [ -z "${JAVA_HOME:-}" ] && [ -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]; then
    JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
    export JAVA_HOME
fi
./mvnw clean package
node --test src/test/js/previas-formularios.test.mjs
if [ "${1:-}" = "--mysql" ]; then
    VALIDAR_MYSQL=true ./mvnw '-Dtest=Mysql*Test' test
fi
