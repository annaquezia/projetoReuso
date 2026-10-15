#!/usr/bin/env python3
"""Valida HTTP → MySQL, reinício real e exclusão de um evento criado pelo próprio script.

Uso: DB_PASSWORD=... python3 scripts/verificar_reinicio.py [--port 8081] [--jar caminho]
Requer conta de centro existente: TEST_CENTRO_EMAIL e TEST_PASSWORD (padrão: contas demo).
Não executa seed nem altera registros anteriores. Logs ficam em target/.
"""
import argparse
import hashlib
import http.cookiejar
import os
import pathlib
import re
import shutil
import socket
import subprocess
import time
import urllib.parse
import urllib.request
import uuid
from datetime import date, timedelta
from html.parser import HTMLParser


class Campos(HTMLParser):
    def __init__(self):
        super().__init__()
        self.valores = {}

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "input" and attrs.get("name"):
            self.valores[attrs["name"]] = attrs.get("value", "")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8081)
    parser.add_argument("--jar", default="target/projeto-reuso-0.3.0.jar")
    args = parser.parse_args()
    raiz = pathlib.Path(__file__).resolve().parents[1]
    jar = (raiz / args.jar).resolve()
    if not jar.is_file():
        raise SystemExit("Empacote a aplicação antes de executar esta verificação.")
    mysql = shutil.which("mysql") or "/usr/local/mysql/bin/mysql"
    java_home = os.environ.get("JAVA_HOME", "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home")
    java = str(pathlib.Path(java_home) / "bin/java") if pathlib.Path(java_home).is_dir() else "java"
    banco = urllib.parse.urlparse(os.environ.get("DB_URL", "jdbc:mysql://127.0.0.1:3306/projetoreuso").removeprefix("jdbc:"))
    esquema = banco.path.strip("/")
    if not re.fullmatch(r"[A-Za-z0-9_]+", esquema):
        raise SystemExit("Nome de banco inválido.")
    ambiente_sql = os.environ.copy()
    ambiente_sql["MYSQL_PWD"] = os.environ.get("DB_PASSWORD", "")

    def sql(consulta):
        resultado = subprocess.run([mysql, "--connect-timeout=5", "--batch", "--skip-column-names",
                                    "-h", banco.hostname or "127.0.0.1", "-P", str(banco.port or 3306),
                                    "-u", os.environ.get("DB_USERNAME", "root"), esquema, "-e", consulta],
                                   env=ambiente_sql, text=True, capture_output=True, check=True)
        return resultado.stdout.strip()

    tabelas = tuple(sql("SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name <> 'flyway_schema_history' ORDER BY table_name").splitlines())

    def resumo_banco():
        return {t: hashlib.sha256(sql("SELECT * FROM " + t + " ORDER BY id").encode()).hexdigest() for t in tabelas}

    with socket.socket() as porta:
        if porta.connect_ex(("127.0.0.1", args.port)) == 0:
            raise SystemExit("A porta está ocupada. Informe --port com uma porta livre.")
    antes = resumo_banco()
    base = "http://127.0.0.1:" + str(args.port)
    processo = None
    log = None
    evento_id = None
    marca = "auditoria-reinicio-" + uuid.uuid4().hex
    cliente = None

    def iniciar():
        nonlocal processo, log, cliente
        ambiente = os.environ.copy()
        # A verificação nunca deve criar contas por seed/bootstrap.
        ambiente.pop("SPRING_PROFILES_ACTIVE", None)
        ambiente["APP_BOOTSTRAP_EMAIL"] = ""
        ambiente["APP_BOOTSTRAP_PASSWORD"] = ""
        log = (raiz / "target/mysql-reinicio.log").open("a")
        processo = subprocess.Popen([java, "-jar", str(jar), "--server.address=127.0.0.1",
                                     "--server.port=" + str(args.port), "--app.demo.seed=false"],
                                    cwd=raiz, env=ambiente, stdout=log, stderr=subprocess.STDOUT)
        cliente = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        limite = time.monotonic() + 50
        while time.monotonic() < limite:
            if processo.poll() is not None:
                raise RuntimeError("A aplicação não iniciou. Consulte target/mysql-reinicio.log.")
            try:
                with cliente.open(base + "/login", timeout=2) as resposta:
                    if resposta.status == 200:
                        return
            except (OSError, urllib.error.URLError):
                time.sleep(0.3)
        raise RuntimeError("Tempo de inicialização excedido.")

    def parar():
        nonlocal processo, log
        if processo is not None:
            processo.terminate()
            try:
                processo.wait(timeout=15)
            except subprocess.TimeoutExpired:
                processo.kill()
                processo.wait(timeout=5)
            processo = None
        if log is not None:
            log.close()
            log = None

    def pagina(caminho, dados=None):
        corpo = None if dados is None else urllib.parse.urlencode(dados).encode()
        with cliente.open(base + caminho, data=corpo, timeout=10) as resposta:
            return resposta.read().decode()

    def postar(formulario, destino, dados):
        campos = Campos()
        campos.feed(pagina(formulario))
        dados["_csrf"] = campos.valores["_csrf"]
        return pagina(destino, dados)

    def login(email, senha):
        pagina_resultante = postar("/login", "/login", {"username": email, "password": senha})
        if "sidebar-greeting" not in pagina_resultante:
            raise RuntimeError("Login falhou: configure TEST_CENTRO_EMAIL/TEST_PASSWORD para uma conta existente.")

    try:
        iniciar()
        senha = os.environ.get("TEST_PASSWORD", "Demo1234")
        centro_email = os.environ.get("TEST_CENTRO_EMAIL", "centro@demo.com")
        login(centro_email, senha)
        dados = {"nome": marca, "descricao": "Verificação de persistência entre processos",
                 "data": str(date.today() + timedelta(days=7)), "horario": "19:00",
                 "local": "Sala inicial", "quantidadeVagas": "2"}
        postar("/centro-academico/eventos/novo", "/centro-academico/eventos/novo", dados)
        encontrado = sql("SELECT id FROM evento WHERE nome = '" + marca + "'")
        if not encontrado.isdigit():
            raise RuntimeError("O evento criado pela interface não foi encontrado no MySQL.")
        evento_id = int(encontrado)
        print("CREATE: registro criado por HTTP e confirmado em SQL.", flush=True)
        rota = "/centro-academico/eventos/" + str(evento_id)
        dados["local"] = "Sala atualizada"
        postar(rota + "/editar", rota + "/editar", dados)
        assert sql("SELECT local FROM evento WHERE id = " + str(evento_id)) == "Sala atualizada"
        print("UPDATE: novo valor confirmado em SQL.", flush=True)
        parar()
        iniciar()
        assert marca in pagina("/eventos/" + str(evento_id))
        assert sql("SELECT local FROM evento WHERE id = " + str(evento_id)) == "Sala atualizada"
        print("READ: registro preservado após encerrar e reiniciar a JVM.", flush=True)
        login(centro_email, senha)
        postar(rota + "/excluir", rota + "/excluir", {})
        assert sql("SELECT COUNT(*) FROM evento WHERE id = " + str(evento_id)) == "0"
        print("DELETE: ausência confirmada em SQL após exclusão pela interface.", flush=True)
    finally:
        parar()
        if evento_id is not None:
            sql("DELETE FROM evento WHERE id = " + str(evento_id) + " AND nome = '" + marca + "'")
    assert resumo_banco() == antes, "O banco diverge do estado anterior. Confira alterações concorrentes."
    print("PRESERVAÇÃO: conteúdo das tabelas idêntico ao anterior ao teste.", flush=True)


if __name__ == "__main__":
    main()
