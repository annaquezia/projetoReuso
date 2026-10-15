#!/usr/bin/env python3
"""Uso: python3 scripts/compactacao.py [--verificar]. Gera/confere o diff da refatoração."""
import argparse
import difflib
import pathlib
import subprocess

raiz = pathlib.Path(__file__).resolve().parents[1]
origem = "src/main/java/br/edu/projetoreuso/usuario/UsuarioService.java"
anterior = raiz / "extras/Compactacao/UsuarioService.antes.txt"
resultado = raiz / "extras/Compactacao/UsuarioService.diff"
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("--verificar", action="store_true")
args = parser.parse_args()
if (raiz / ".git").exists():
    original_git = subprocess.check_output(["git", "show", "f57f9c7:" + origem], cwd=raiz, text=True)
else:
    import hashlib
    original_git = anterior.read_text()
    if hashlib.sha256(anterior.read_bytes()).hexdigest() != "305d6d86546ccef3f9640cfeee3c947af75e8d15881a8c7f439c9d8b62866e81":
        raise SystemExit("Original empacotado diverge do SHA-256 registrado na preparação da entrega.")
if anterior.read_text() != original_git:
    raise SystemExit("A cópia anterior diverge da base Git f57f9c7.")
diff = "".join(difflib.unified_diff(original_git.splitlines(keepends=True),
                                  (raiz / origem).read_text().splitlines(keepends=True),
                                  fromfile="f57f9c7/" + origem, tofile="atual/" + origem))
if args.verificar:
    if resultado.read_text() != diff:
        raise SystemExit("Diff desatualizado: execute novamente sem --verificar.")
    print("Original verificado (Git ou SHA-256 da entrega); diff corresponde ao código atual.")
else:
    resultado.write_text(diff)
    print("Diff gerado em extras/Compactacao/UsuarioService.diff.")
