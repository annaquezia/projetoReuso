# Compactação por reuso — Extract Method

## Antes e depois verificáveis

Base original: commit `f57f9c7`, arquivo
`src/main/java/br/edu/projetoreuso/usuario/UsuarioService.java`.
A cópia integral está em [compactacao/UsuarioService.antes.txt](UsuarioService.antes.txt).
O resultado está no serviço atual; o diff integral está em
[compactacao/UsuarioService.diff](UsuarioService.diff).

Antes, `atualizarGerenciado` e `atualizarMinhaConta` repetiam validações de e-mail,
tipo e senha, preenchimento e hash opcional. Um reparo em apenas um método podia
deixar a outra tela com uma regra diferente. Exemplo literal do original:

```java
// Em atualizarGerenciado:
preencherBase(alvo, form);
preencherPerfil(alvo, form);
if (StringUtils.hasText(form.getSenha())) alvo.setSenha(passwordEncoder.encode(form.getSenha()));

// Em atualizarMinhaConta:
preencherBase(alvo, form);
preencherPerfil(alvo, form);
if (StringUtils.hasText(form.getSenha())) {
    if (form.getSenha().length() < 8) throw new IllegalArgumentException("A senha precisa ter pelo menos 8 caracteres.");
    alvo.setSenha(passwordEncoder.encode(form.getSenha()));
}
```

Depois, as duas entradas buscam o usuário apropriado, delegam as regras comuns e
salvam. O trecho abaixo está no código final:

```java
@Transactional
public Usuario atualizarGerenciado(String emailAtor, Long id, UsuarioForm form) {
    Usuario alvo = buscarGerenciado(emailAtor, id);
    aplicarAtualizacao(alvo, form);
    return usuarios.save(alvo);
}

@Transactional
public void atualizarMinhaConta(String emailAtor, UsuarioForm form) {
    Usuario alvo = usuarioPorEmail(emailAtor);
    aplicarAtualizacao(alvo, form);
    usuarios.save(alvo);
}
```

`aplicarAtualizacao` concentra validação, preenchimento e hash. A permissão de
alterar outro usuário permanece em `buscarGerenciado`. Senha vazia continua
preservando o hash; senha informada exige tamanho mínimo e é codificada.
As duas operações continuam transacionais.

Redução verificável no serviço: `existsByEmailIgnoreCaseAndIdNot`, comparação do
tipo, chamada `preencherPerfil(alvo, form)` e codificação opcional de `alvo` passam,
cada um, de duas ocorrências para uma. São quatro cópias redundantes eliminadas.
A correção de matrícula única/vazia é adicional; não se afirma redução do total
de linhas do arquivo, que também recebeu exclusão protegida.

## Reproduzir e validar

```sh
python3 scripts/compactacao.py --verificar
# Depois de editar esse serviço, atualizar o registro deliberadamente:
python3 scripts/compactacao.py
sh scripts/validar.sh
```

O script verifica a cópia anterior contra o Git e compara o diff com o arquivo
atual. `RequisitosCrudTest` comprova edição gerenciada, sessão da própria conta,
preservação de senha, rejeição de e-mail duplicado e rollback; os testes MySQL
confirmam atualização persistida. A captura anterior não contém credenciais.

## Vídeo entregue e reprodução

[Compactação por Reuso — MP4](<video/Compactacao por Reuso.mp4>):
**2min42s**, 1600 × 900, narração em português. Mostra cinco passos: repetição no
original, extração do método, duas chamadas resultantes, relatórios reais de testes
e verificação do antes/depois. É uma apresentação narrada com capturas do código
real; não é uma gravação de digitação ao vivo. A decodificação integral do vídeo
e do áudio concluiu sem erros.

`extras/Compactacao/Ferramentas/gerar_video_compactacao.py --preparar` lê o original, o serviço atual e os
relatórios aprovados, gerando as páginas e os textos em `extras/Compactacao/video/`.
Após capturar essas páginas como `quadro-1.jpg` a `quadro-5.jpg`, execute
`python3 extras/Compactacao/Ferramentas/gerar_video_compactacao.py --montar --ffmpeg /caminho/ffmpeg`.
A montagem usa o comando `say` do macOS, voz Luciana, e FFmpeg; não são dependências
da aplicação Java. Os arquivos intermediários ficam em `target/`.

## Roteiro para uma demonstração manual adicional (aproximadamente 4 minutos)

1. **0:00–0:30:** apresentar o problema: duas telas atualizam usuário e repetiam regras.
2. **0:30–1:20:** abrir o original e localizar `atualizarGerenciado`/`atualizarMinhaConta`.
3. **1:20–2:10:** abrir o serviço atual e explicar a extração `aplicarAtualizacao`.
   Mostrar que autorização e transações continuam nas entradas.
4. **2:10–2:40:** abrir o diff e apontar as quatro famílias de repetição eliminadas.
5. **2:40–3:30:** editar um palestrante e a própria conta; consultar as alterações no MySQL.
6. **3:30–4:00:** mostrar testes aprovados e a verificação do original/diff; citar
   preservação do comportamento e o elemento reutilizável.

A versão revisada permanece **somente local**. Em 15/10/2026, o usuário dispensou
novas gravações e publicações; não há envio pendente.

A [publicação anterior, não listada](https://youtu.be/7V2N4tl6JGI), foi preservada.
Ela demonstra o mesmo Extract Method, mas seu quadro de validação corresponde ao
escopo anterior. A [captura da confirmação](../Evidencias/youtube-publicado.png)
registra aquela publicação, não o MP4 revisado local.
