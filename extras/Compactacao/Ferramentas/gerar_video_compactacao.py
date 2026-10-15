#!/usr/bin/env python3
"""Gera o guia visual do código real e monta o MP4 narrado a partir de cinco capturas.
Uso: python3 extras/Compactacao/Ferramentas/gerar_video_compactacao.py --preparar
     python3 extras/Compactacao/Ferramentas/gerar_video_compactacao.py --montar --ffmpeg /caminho/ffmpeg
Capturas esperadas: extras/Compactacao/video/quadro-1.jpg ... quadro-5.jpg (panorâmicas).
"""
import argparse
import html
import json
import pathlib
import shutil
import subprocess
import xml.etree.ElementTree as ET

RAIZ = pathlib.Path(__file__).resolve().parents[3]
DESTINO = RAIZ / 'extras/Compactacao/video'


def metodo(codigo, nome):
    inicio = codigo.index('    public ', codigo.index(nome) - 40) if nome != 'aplicarAtualizacao' else codigo.index('    private void aplicarAtualizacao')
    abertura = codigo.index('{', inicio)
    nivel = 1
    fim = abertura + 1
    while nivel:
        if codigo[fim] == '{': nivel += 1
        elif codigo[fim] == '}': nivel -= 1
        fim += 1
    return codigo[inicio:fim].strip()


def preparar():
    DESTINO.mkdir(parents=True, exist_ok=True)
    antes = (RAIZ/'extras/Compactacao/UsuarioService.antes.txt').read_text()
    depois = (RAIZ/'src/main/java/br/edu/projetoreuso/usuario/UsuarioService.java').read_text()
    relatorios = list((RAIZ/'target/surefire-reports').glob('TEST-*.xml'))
    totais = {k: sum(int(ET.parse(p).getroot().attrib[k]) for p in relatorios) for k in ['tests','errors','failures','skipped']}
    if not relatorios or any(totais[k] for k in ['errors','failures','skipped']):
        raise SystemExit('Execute e aprove a validação Java e MySQL antes de preparar o vídeo.')
    verificado = subprocess.check_output(['python3','scripts/compactacao.py','--verificar'],cwd=RAIZ,text=True).strip()
    partes = [
        ('1. Identificar a repetição', [metodo(antes,'atualizarGerenciado'), metodo(antes,'atualizarMinhaConta')],
         'Original preservado no Git: f57f9c7 · UsuarioService.java',
         'Esta é a compactação por reuso do Conecta Campus. No código original, duas telas atualizavam usuários: a edição gerenciada e a própria conta. Os métodos repetiam validação de e-mail, tipo e senha, preenchimento de atributos e codificação da senha. A cópia original foi preservada e conferida contra o histórico Git. O problema é manter a mesma regra em dois lugares.'),
        ('2. Extrair as regras comuns', [metodo(depois,'aplicarAtualizacao')],
         'Extract Method: uma responsabilidade reutilizada por duas entradas',
         'A técnica aplicada foi Extract Method. A rotina aplicarAtualizacao concentra as regras compartilhadas, o preenchimento do perfil e a codificação opcional da senha. Senha vazia preserva o hash anterior. A validação de matrícula foi acrescentada como correção. A redução demonstrada é de duplicação; não afirmamos que o arquivo inteiro ficou com menos linhas.'),
        ('3. Reutilizar nas duas operações', [metodo(depois,'atualizarGerenciado'), metodo(depois,'atualizarMinhaConta')],
         'Permissão e contexto continuam nas entradas · transações preservadas',
         'Agora cada operação identifica o usuário correto, chama a rotina compartilhada e salva. A edição gerenciada continua verificando a permissão em buscarGerenciado. A própria conta continua partindo da identidade autenticada. Os métodos públicos mantêm a transação. Assim, centralizamos somente o comportamento comum, preservando as diferenças de autorização e o fluxo das duas telas.'),
        ('4. Verificar o comportamento', [json.dumps(totais, ensure_ascii=False, indent=2),
         'RequisitosCrudTest\n  edição gerenciada\n  própria conta e sessão\n  senha preservada e duplicidade recusada\n  rollback\n\nMysqlPersistenciaTest\n  criar → consultar SQL → atualizar → consultar SQL\n  excluir → confirmar ausência no banco\n\nComunicadosTest / MysqlComunicadosTest\n  comunicação pública e preservação dos vínculos'],
         'Relatórios reais de target/surefire-reports · telas de usuários e eventos',
         'Depois da mudança, executamos os testes das duas edições, preservação de senha, rejeição de duplicidade, sessão e rollback. Também executamos os ciclos CRUD no MySQL com consultas SQL depois de cada operação. Os relatórios exibidos foram lidos dos arquivos reais de testes. A aplicação ainda foi encerrada e reiniciada para comprovar persistência. Um resultado visual sozinho não comprova alteração no banco.'),
        ('5. Conferir a evidência e defender o reuso', [verificado,
         'python3 scripts/compactacao.py --verificar\n\nANTES: duas implementações das mesmas regras\nDEPOIS: duas chamadas a aplicarAtualizacao\n\nQuatro famílias de repetição consolidadas:\n• e-mail único\n• tipo de usuário\n• preenchimento do perfil\n• hash opcional da senha'],
         'Original + diff verificável + testes · compactação não é minificação',
         'Por fim, o verificador compara o original com o commit-base e o diff com o código atual. Quatro famílias de repetição passaram de duas ocorrências para uma. O elemento reutilizável é aplicarAtualizacao. A manutenção de uma regra comum passa a ocorrer nesse ponto único. Esta apresentação mostra o processo com quadros do código real e narração, e não pretende ser uma gravação de digitação ao vivo.')
    ]
    capitulos=[]
    for i,(titulo,blocos,rodape,narracao) in enumerate(partes,1):
        texto=''.join('<pre>'+html.escape(b)+'</pre>' for b in blocos)
        pagina='''<!doctype html><html lang="pt-BR"><meta charset="utf-8"><title>Compactação por reuso</title><style>
*{box-sizing:border-box}body{margin:0;background:#161620;color:#f7f4ff;font-family:Arial,sans-serif;padding:42px 58px;width:1600px;height:900px}header{font-size:15px;letter-spacing:2px;color:#db8cad}h1{font-size:38px;margin:16px 0 28px}.codigo{display:flex;gap:25px;height:650px}pre{flex:1;min-width:0;margin:0;border:1px solid #65566b;border-radius:14px;background:#232330;padding:22px;font:18px/1.35 Menlo,monospace;white-space:pre-wrap;overflow-wrap:anywhere}footer{font-size:19px;color:#dbc6d9;margin-top:22px}</style><body><header>CONECTA CAMPUS · REUSO DE SOFTWARE</header><h1>'''+html.escape(titulo)+'</h1><div class="codigo">'+texto+'</div><footer>'+html.escape(rodape)+'</footer></body></html>'
        (DESTINO/f'passo-{i}.html').write_text(pagina)
        (DESTINO/f'narracao-{i}.txt').write_text(narracao)
        capitulos.append({'quadro':f'quadro-{i}.jpg','texto':f'narracao-{i}.txt','audio':f'narracao-{i}.aiff'})
    (DESTINO/'capitulos.json').write_text(json.dumps(capitulos,ensure_ascii=False,indent=2))
    print('Cinco páginas preparadas a partir do original, código atual e relatórios verificados.')


def montar(ffmpeg):
    partes=[]
    temporario=RAIZ/'target/video-compactacao'
    temporario.mkdir(parents=True,exist_ok=True)
    for i,capitulo in enumerate(json.loads((DESTINO/'capitulos.json').read_text()),1):
        quadro=DESTINO/capitulo['quadro']; audio=temporario/capitulo['audio']
        if not quadro.is_file():raise SystemExit('Captura ausente: '+str(quadro))
        subprocess.run(['say','-v','Luciana','-r','155','-f',str(DESTINO/capitulo['texto']),'-o',str(audio)],check=True)
        parte=temporario/f'parte-{i}.mp4'
        subprocess.run([ffmpeg,'-y','-loglevel','error','-loop','1','-framerate','10','-i',str(quadro),'-i',str(audio),
                        '-vf','scale=1600:900,format=yuv420p','-c:v','libx264','-preset','fast','-tune','stillimage',
                        '-c:a','aac','-b:a','128k','-shortest','-movflags','+faststart',str(parte)],check=True)
        partes.append(parte)
    lista=temporario/'partes.txt'
    lista.write_text(''.join("file '"+str(p)+"'\n" for p in partes))
    saida=DESTINO/'Compactacao por Reuso.mp4'
    subprocess.run([ffmpeg,'-y','-loglevel','error','-f','concat','-safe','0','-i',str(lista),'-c','copy','-movflags','+faststart',str(saida)],check=True)
    print(saida)


if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__)
    acao=p.add_mutually_exclusive_group(required=True)
    acao.add_argument('--preparar',action='store_true');acao.add_argument('--montar',action='store_true')
    p.add_argument('--ffmpeg',default=shutil.which('ffmpeg'))
    args=p.parse_args()
    if args.preparar:preparar()
    elif not args.ffmpeg:p.error('Informe --ffmpeg com o executável disponível.')
    else:montar(args.ffmpeg)
