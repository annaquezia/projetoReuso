#!/usr/bin/env python3
"""Uso: python3 scripts/empacotar.py [--sem-compilar]. Produz ZIP e SHA-256 em extras/Entrega."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import zipfile

RAIZ = Path(__file__).resolve().parents[1]
JAR = RAIZ / 'target/projeto-reuso-0.3.0.jar'
DESTINO = RAIZ / 'extras/Entrega/Projeto Reuso.zip'
PASTA = 'Projeto Reuso'


def hash_arquivo(caminho):
    digest = hashlib.sha256()
    with caminho.open('rb') as arquivo:
        for parte in iter(lambda: arquivo.read(1024 * 1024), b''):
            digest.update(parte)
    return digest.hexdigest()


def arquivos_entrega():
    arquivos = [RAIZ / nome for nome in ['README.md', 'pom.xml', 'mvnw', 'mvnw.cmd', '.gitignore']]
    for pasta in ['src', '.mvn', 'scripts', 'extras']:
        for arquivo in (RAIZ / pasta).rglob('*'):
            relativo = arquivo.relative_to(RAIZ)
            if not arquivo.is_file() or arquivo.is_symlink():
                continue
            if relativo.parts[:2] == ('extras', 'Entrega'):
                continue
            if any(p in {'.DS_Store', '__pycache__', '.git'} or p.startswith('.env') for p in relativo.parts):
                continue
            if arquivo.suffix in {'.pyc', '.log'}:
                continue
            arquivos.append(arquivo)
    return sorted(arquivos)


def verificar_jar():
    if not JAR.exists():
        raise SystemExit('JAR ausente. Execute sem --sem-compilar para compilar.')
    with zipfile.ZipFile(JAR) as jar:
        for arquivo in (RAIZ / 'src/main/resources').rglob('*'):
            if not arquivo.is_file() or arquivo.name == '.DS_Store':
                continue
            caminho = 'BOOT-INF/classes/' + arquivo.relative_to(RAIZ / 'src/main/resources').as_posix()
            if jar.read(caminho) != arquivo.read_bytes():
                raise SystemExit(f'JAR desatualizado: {arquivo.relative_to(RAIZ)}. Compile novamente.')
    if any(p.stat().st_mtime > JAR.stat().st_mtime for p in (RAIZ / 'src/main/java').rglob('*.java')):
        raise SystemExit('Há código Java posterior ao JAR. Compile novamente.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--sem-compilar', action='store_true', help='usa o JAR já gerado, conferindo recursos e datas')
    args = parser.parse_args()
    if not args.sem_compilar:
        wrapper = RAIZ / ('mvnw.cmd' if os.name == 'nt' else 'mvnw')
        subprocess.run([str(wrapper), 'package'], cwd=RAIZ, check=True)
    verificar_jar()
    arquivos = arquivos_entrega()
    manifesto = {p.relative_to(RAIZ).as_posix(): hash_arquivo(p) for p in arquivos}
    manifesto['projeto-reuso.jar'] = hash_arquivo(JAR)
    DESTINO.parent.mkdir(parents=True, exist_ok=True)
    temporario = DESTINO.with_suffix('.zip.tmp')
    try:
        with zipfile.ZipFile(temporario, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as pacote:
            for arquivo in arquivos:
                pacote.write(arquivo, f'{PASTA}/{arquivo.relative_to(RAIZ).as_posix()}')
            pacote.write(JAR, f'{PASTA}/projeto-reuso.jar')
            pacote.writestr(f'{PASTA}/MANIFESTO.json', json.dumps(manifesto, ensure_ascii=False, indent=2) + '\n')
        with zipfile.ZipFile(temporario) as pacote:
            if pacote.testzip():
                raise SystemExit('Arquivo corrompido na entrega.')
            for relativo, digest in manifesto.items():
                if hashlib.sha256(pacote.read(f'{PASTA}/{relativo}')).hexdigest() != digest:
                    raise SystemExit(f'Conteúdo divergente no ZIP: {relativo}')
        temporario.replace(DESTINO)
    finally:
        temporario.unlink(missing_ok=True)
    DESTINO.with_suffix('.zip.sha256').write_text(f'{hash_arquivo(DESTINO)}  {DESTINO.name}\n')
    print(f'Entrega verificada: {DESTINO.relative_to(RAIZ)} ({DESTINO.stat().st_size / 1024**2:.1f} MiB; {len(manifesto)} arquivos).')
    print('Inclui código, JAR, iniciadores e extras; exclui Git, dados do MySQL, credenciais, IDE e caches.')


if __name__ == '__main__':
    main()
