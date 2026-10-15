# Conecta Campus — eventos acadêmicos

Aplicação web para universidade, centros acadêmicos, palestrantes e estudantes.
A universidade gerencia centros; cada centro gerencia palestrantes e eventos;
estudantes consultam a programação e fazem/cancelam inscrições.
A interface mantém a identidade visual PoliWeek.

## Equipe

**Laura, Otavio e Anna**: três integrantes, com **seis telas distintas de operações
CRUD**, conforme esclarecido pela equipe. Não são necessários seis cadastros completos.
A autoria histórica não foi atribuída; [DEFESA.md](extras/Documentacao/DEFESA.md) contém uma divisão sugerida de estudo.

## Tecnologias e organização

Java 17, Spring Boot 3.5.16, Spring MVC, Thymeleaf, Spring Security,
Bean Validation, JPA/Hibernate, MySQL e Flyway. Maven Wrapper incluído.

| Diretório em `src/main` | Responsabilidade |
|---|---|
| `resources/templates`, `resources/static` | HTML renderizado pelo servidor, CSS e ponte JavaScript das prévias |
| `java/br/edu/projetoreuso/web` | Rotas HTTP, formulários e respostas |
| `java/br/edu/projetoreuso/{usuario,evento,inscricao}` | Serviços transacionais e repositórios JPA; entidade `InscricaoEvento` |
| `java/usuario`, `java/evento` | Entidades de domínio e herança dos usuários |
| `java/evento/state`, `java/inscricao/state` | Estados de eventos e inscrições |
| `java/br/edu/projetoreuso/patterns/observer` | Publicador e observadores das duas prévias |
| `java/br/edu/projetoreuso/organizacao` | Comunicados: entidade, regras e repositório; verificação do centro proprietário |
| `java/br/edu/projetoreuso/config` | Segurança, inicialização opcional e variante do produto |
| `resources/db/migration` | Migrações MySQL V1–V5; não alterar versões já aplicadas |

## Executar com MySQL

1. Tenha **Java 17** e um servidor MySQL compatível em execução.
2. Gere o JAR com `./mvnw package` (Windows: `./mvnw.cmd package`).
3. Execute `bash scripts/iniciar.sh --seed` no macOS/Linux ou
   `./scripts/iniciar.ps1 -Seed` no Windows/PowerShell.
4. Informe suas próprias credenciais do MySQL e abra
   [http://127.0.0.1:8080](http://127.0.0.1:8080).
   As próximas execuções dispensam `--seed` / `-Seed`.

Os iniciadores usam `reuso_avaliacao` como banco separado. Respeitam `DB_URL`,
`DB_USERNAME` e `DB_PASSWORD` já definidos; não leem `.env` nem gravam a senha.
Executar diretamente `java -jar target/projeto-reuso-0.3.0.jar` usa as variáveis
ou os padrões de `application.properties` (banco `projetoreuso`).
Flyway aplica as migrações pendentes e Hibernate valida as entidades.

A entrega para o professor é gerada com `python3 scripts/empacotar.py`, em
`extras/Entrega/Projeto Reuso.zip`. Inclui o JAR pronto, código e documentação;
não inclui `.git`, suas credenciais ou dados do MySQL. O professor usa a própria
senha do banco e não precisa instalar ferramentas de compilação para executar.
O [guia de entrega](extras/Documentacao/ENTREGA.md) detalha empacotamento,
pré-requisitos, Windows/macOS/Linux e a demonstração do reuso nas telas reais.

Se o banco ainda não tiver uma universidade, informe `APP_BOOTSTRAP_EMAIL` e
`APP_BOOTSTRAP_PASSWORD` (mínimo oito caracteres) antes de iniciar. O bootstrap
não altera contas existentes. Para a carga **opcional** de quatro contas fictícias
e três eventos no MySQL, execute uma vez:

```sh
java -jar target/projeto-reuso-0.3.0.jar --spring.profiles.active=seed --app.demo.seed=true
```

| Perfil da carga opcional | E-mail | Senha fictícia |
|---|---|---|
| Universidade | `universidade@demo.com` | `Demo1234` |
| Centro acadêmico | `centro@demo.com` | `Demo1234` |
| Estudante | `usuario@demo.com` | `Demo1234` |
| Palestrante | `palestrante@demo.com` | `Demo1234` |

Contas existentes mantêm sua senha. A carga é independente da execução normal.
A credencial fixa encontrada foi retirada da configuração atual; o histórico Git
foi preservado e ainda contém a versão antiga. A rotação dessa credencial não foi executada.

## CRUD e persistência

O requisito é atendido por estas **seis telas**, que representam operações distintas
de usuários e eventos. As demais operações válidas dos dois cadastros permanecem disponíveis.

| Tela | Operação | Rota | Template |
|---|---|---|---|
| Novo usuário | C | `/usuarios/novo` | `usuarios/novo.html` |
| Lista de usuários | R | `/usuarios` | `usuarios/lista.html` |
| Editar usuário | U | `/usuarios/{id}/editar` | `usuarios/editar.html` |
| Novo evento | C | `/centro-academico/eventos/novo` | `eventos/novo.html` |
| Lista de eventos | R | `/centro-academico/eventos` | `eventos/gestao-lista.html` |
| Excluir evento | D | `/centro-academico/eventos/{id}/excluir` | `excluir.html` |

GET apresenta a tela; POST autenticado com CSRF grava a alteração. A listagem lê os
dados do MySQL. Usuários/eventos têm C/R/U/D completos, mas cada operação acima conta
como uma tela na avaliação. A confirmação de exclusão não apaga nada antes do POST.
Registros com vínculos são protegidos: use **Desativar** ou **Arquivar** para manter o histórico.

Entre como centro para gerenciar palestrantes e eventos; como universidade para
centros acadêmicos. O menu **Eventos** do centro abre a gestão; **Ver programação**
abre o catálogo. Listas viram cartões identificados no celular. Confirmações
abrem em modal e mantêm a tela de confirmação com URL própria. Mensagens de
resultado usam toasts; erros de campos permanecem junto ao formulário.
Navegação, ícones Bootstrap locais e componentes de apresentação são compartilhados.

**Comunicados** foi mantido porque já contém dados em uso e aparece nos detalhes
públicos dos eventos. Esse cadastro não é necessário para compor as seis telas.
Categorias, espaços e presenças estavam vazios/sem vínculos e foram retirados.
V5 aborta se encontrar dados nessas estruturas; as migrações já aplicadas continuam no histórico.

Para comprovar persistência, crie um registro identificável, consulte seu ID no MySQL,
edite e consulte novamente; exclua somente o registro de teste e confirme sua ausência.
`MysqlPersistenciaTest` cobre usuários/eventos; `MysqlComunicadosTest` cobre o conteúdo preservado.

## Seis exemplos de padrões e LPS

- **Singleton GoF:** os estados de evento e inscrição são instâncias imutáveis com construtor privado e `getInstance()`. Dois exemplos usados na defesa: `DisponivelState` e `InscricaoAtivaState`. O contexto recebe o comportamento compartilhado sem compartilhar seus dados. Os serviços continuam gerenciados pelo Spring.
- **Observer:** prévia de usuário e prévia de evento, com `FormularioSubject`, `PreviaUsuarioObserver` e `PreviaEventoObserver`. `PreviasController` registra e notifica objetos Java; o JavaScript transporta dados e apresenta texto, sem persistir rascunhos.
- **State:** `Evento` delega a `evento/state`; `InscricaoEvento` delega a `inscricao/state`, inclusive após carregar do banco. Cancelamento e reinscrição usam os estados no fluxo real.
- **Variabilidade LPS:** `APP_INSCRICOES_HABILITADAS=true` oferece catálogo com inscrições; `false` oferece somente divulgação. `VarianteProduto` controla os serviços e a interface. Usuários, eventos e persistência são o núcleo comum. A seleção ocorre ao iniciar, preservando os registros existentes.
- **Compactação:** Extract Method em `UsuarioService.aplicarAtualizacao`, compartilhado pela edição gerenciada e pela própria conta. [Antes e depois verificáveis](extras/Compactacao/README.md).

## Verificações reproduzíveis

```sh
sh scripts/validar.sh
# Com MySQL configurado no ambiente:
sh scripts/validar.sh --mysql
python3 scripts/verificar_reinicio.py --port 8081
python3 scripts/compactacao.py --verificar
```

O primeiro comando compila, testa e empacota; Node é necessário para os quatro
testes da ponte JavaScript. O modo `--mysql` cria e remove somente seus próprios
registros de teste no esquema existente. A verificação de reinício inicia duas JVMs
sucessivas, cria/edita/exclui um evento por HTTP e compara o conteúdo das oito tabelas.
Ela requer uma conta de centro existente (`TEST_CENTRO_EMAIL` e `TEST_PASSWORD`;
padrão: centro da carga fictícia) e uma porta livre. Logs e relatórios ficam em `target/`.

O perfil `demo` continua disponível para demonstração temporária com H2:
`java -jar target/projeto-reuso-0.3.0.jar --spring.profiles.active=demo --server.port=8081`.
Ele não comprova persistência em MySQL e apaga seus dados ao encerrar.

Consulte [DEFESA.md](extras/Documentacao/DEFESA.md), [AUDITORIA.md](extras/Documentacao/AUDITORIA.md) e
[extras/Evidencias/README.md](extras/Evidencias/README.md). O [vídeo local da compactação](<extras/Compactacao/video/Compactacao por Reuso.mp4>)
revisado fica somente local, conforme decisão do usuário em 15/10/2026. Não há
nova gravação ou publicação pendente. A [publicação anterior, não listada](https://youtu.be/7V2N4tl6JGI),
foi preservada e contém o quadro de validação do escopo anterior.
