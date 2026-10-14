# Design patterns em Java aplicados às oito telas CRUD

Atualizado em **09/10/2026**. O requisito é oito telas ligadas aos dois recursos do MVP: Usuário e Evento. Auditoria, métricas e os Singletons de relógio/contadores foram retirados. Os padrões abaixo participam dos fluxos usados pelas telas.

## 1. Mapa dos padrões

| Conceito | Onde é aplicado | Efeito na tela |
|---|---|---|
| Observer 1 | `PreviaUsuarioObserver`, em Java | Atualiza o resumo de nome, perfil, e-mail e curso/área antes de salvar |
| Observer 2 | `PreviaEventoObserver`, em Java | Atualiza a apresentação de título, descrição, agenda, local e vagas antes de salvar |
| Singleton 1 | Bean Java `UsuarioService` do Spring | Compartilha o serviço que atende às telas de usuário |
| Singleton 2 | Bean Java `EventoService` do Spring | Compartilha o serviço que atende às telas de evento |
| State 1 | `EventoState` com `DisponivelState`, `LotadoState`, `CanceladoState`, `FinalizadoState` | Controla as transições de estado do evento; cada estado permite ou rejeita ações como lotar, cancelar e finalizar |
| State 2 | `InscricaoState` com `InscricaoAtivaState`, `InscricaoCanceladaState` | Controla as transições de estado da inscrição; cancelar e reativar dependem do estado atual |

**Distinção para a apresentação:** os dois serviços são singletons gerenciados pelo Spring. Não são Singletons GoF explícitos com construtor privado e `getInstance()`. Essa implementação não deve ser apresentada como se tivesse essas características. O projeto usa injeção de dependências em vez de criar objetos globais extras apenas para atingir uma contagem.

## 2. Oito telas CRUD

| Nº | Tela | Operação | Rota GET | Arquivo |
|---|---|---|---|---|
| 1 | Listar usuários | R | `/usuarios` | [usuarios/lista.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/usuarios/lista.html>) |
| 2 | Cadastrar usuário | C | `/usuarios/novo` | [usuarios/novo.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/usuarios/novo.html>) |
| 3 | Editar usuário | U | `/usuarios/{id}/editar` | [usuarios/editar.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/usuarios/editar.html>) |
| 4 | Desativar usuário | D lógico | `/usuarios/{id}/desativar` | [usuarios/desativar.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/usuarios/desativar.html>) |
| 5 | Listar eventos | R | `/centro-academico/eventos` | [eventos/gestao-lista.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/eventos/gestao-lista.html>) |
| 6 | Cadastrar evento | C | `/centro-academico/eventos/novo` | [eventos/novo.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/eventos/novo.html>) |
| 7 | Editar evento | U | `/centro-academico/eventos/{id}/editar` | [eventos/editar.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/eventos/editar.html>) |
| 8 | Arquivar evento | D lógico | `/centro-academico/eventos/{id}/arquivar` | [eventos/arquivar.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/eventos/arquivar.html>) |

Cadastrar e editar têm arquivos e rotas próprios, reutilizando os campos em `usuarios/form.html` e `eventos/form.html`. As duas telas de exclusão lógica mostram uma confirmação; abrir a tela não muda dados. O POST com CSRF aplica a alteração.

Universidade administra seus centros acadêmicos; centro acadêmico administra seus palestrantes e eventos. Uma conta de centro acadêmico pode demonstrar as oito telas. Login, cadastro público, painel e detalhes públicos não entram nessa contagem.

## 3. Os dois Observers estão no Java

O contrato é [FormularioObserver<T>](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/FormularioObserver.java:5>), com `atualizar(T dados)`. O objeto observado é [FormularioSubject<T>](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/FormularioSubject.java:9>):

- `registrarObserver(observer)` registra **um objeto Java** como observador da prévia. Esse método não cadastra um aluno em um evento e não é uma ação de inscrição disponível na interface.
- `removerObserver(observer)` remove sua inscrição.
- `alterar(dados)` notifica os inscritos pela interface, sem conhecer os tipos concretos.

### Observer de usuário

[PreviaUsuarioObserver](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/PreviaUsuarioObserver.java:6>) implementa `FormularioObserver<DadosUsuarioPrevia>`. Ao receber os dados, calcula o perfil por extenso e os textos mostrados na prévia da tela. Campos vazios recebem textos de orientação.

O estado de entrada é [DadosUsuarioPrevia](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/DadosUsuarioPrevia.java:4>). Senha e token não são campos desse objeto.

### Observer de evento

[PreviaEventoObserver](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/PreviaEventoObserver.java:10>) implementa `FormularioObserver<DadosEventoPrevia>`. Ao receber os dados, formata data e horário, monta a capacidade em vagas e os textos mostrados no cartão de prévia.

O estado de entrada é [DadosEventoPrevia](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/patterns/observer/DadosEventoPrevia.java:4>). Ele aceita strings de um rascunho incompleto; a prévia não executa a validação de gravação nem persiste o rascunho.

### Walkthrough da tela até o Observer Java

1. A pessoa altera os campos do formulário.
2. [previas-formularios.js](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/static/js/previas-formularios.js>) envia os campos da prévia por HTTP, com um pequeno intervalo para agrupar alterações. O token CSRF segue no cabeçalho; senha e token não entram nos dados observados.
3. [PreviasController](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/web/PreviasController.java:11>) cria um Subject e inscreve o Observer correspondente, em Java.
4. `subject.alterar(dados)` chama `observer.atualizar(dados)`. O Observer calcula o conteúdo da prévia.
5. O controlador devolve o resultado em JSON. O JavaScript apenas coloca os textos recebidos nos elementos da tela; ele não implementa o padrão Observer nem calcula perfil, agenda ou vagas.

| Tela | Endpoint POST da prévia |
|---|---|
| Cadastrar/editar usuário gerenciado | `/usuarios/previa` |
| Minha conta | `/minha-conta/previa` |
| Cadastrar/editar evento | `/centro-academico/eventos/previa` |

As rotas respeitam os perfis e exigem CSRF. Subject e Observer são criados por requisição, para que um rascunho não fique compartilhado entre usuários. Não há sessão de observação permanente ou WebSocket: cada alteração enviada pela tela aciona o fluxo Java acima.

A prévia não grava no banco. Uma resposta antiga não substitui a prévia mais recente. Se a requisição falhar, a tela informa o problema e continua permitindo salvar pelo formulário normal.

## 4. State no ciclo de vida do evento e da inscrição

O padrão State encapsula as regras de transição de estado em objetos separados. Cada estado concreto decide quais ações são válidas e qual será o próximo estado.

### State de evento

A interface [EventoState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/state/EventoState.java:5) declara `lotar(Evento)`, `cancelar(Evento)` e `finalizar(Evento)`. Os quatro estados concretos são:

| Estado | `lotar()` | `cancelar()` | `finalizar()` |
|---|---|---|---|
| [DisponivelState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/state/DisponivelState.java:5) | → Lotado | → Cancelado | → Finalizado |
| [LotadoState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/state/LotadoState.java:5) | ✗ já lotado | → Cancelado | → Finalizado |
| [CanceladoState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/state/CanceladoState.java:5) | ✗ | ✗ já cancelado | ✗ |
| [FinalizadoState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/state/FinalizadoState.java:5) | ✗ | ✗ | ✗ já finalizado |

[Evento](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/evento/Evento.java:33) mantém o state como campo `@Transient`. O `@PostLoad` restaura o objeto de estado a partir do enum `EventoStatus` persistido no banco. `setEventoState()` atualiza o campo transiente e sincroniza o enum para gravação.

### State de inscrição

A interface [InscricaoState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/inscricao/state/InscricaoState.java:5) declara `cancelar(Inscricao)` e `reativar(Inscricao)`. Os dois estados concretos são:

| Estado | `cancelar()` | `reativar()` |
|---|---|---|
| [InscricaoAtivaState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/inscricao/state/InscricaoAtivaState.java:5) | → Cancelada | ✗ já ativa |
| [InscricaoCanceladaState](/Users/otavioaugusto/My Projects/projetoReuso/src/main/java/inscricao/state/InscricaoCanceladaState.java:5) | ✗ já cancelada | → Ativa |

Transições inválidas lançam `IllegalStateException`, impedindo operações fora da ordem esperada.

## 5. Fluxos de criação e atualização

Os fluxos de criação e atualização de usuários e eventos são implementados diretamente nos métodos dos serviços (`criarGerenciado()`, `atualizarGerenciado()`, `criar()`, `atualizar()`), sem uso do padrão Template Method. Cada método realiza validação, montagem e gravação de forma sequencial e explícita.

## 6. Singleton nas operações das telas

- [UsuarioService](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/usuario/UsuarioService.java:21>): bean `@Service`, injetado em `UsuariosController` e `HomeController`.
- [EventoService](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/evento/EventoService.java:24>): bean `@Service`, injetado em `EventoGestaoController`, `EventosController` e `HomeController`.

O escopo padrão mantém uma instância por definição de bean no contexto Spring. Os controladores recebem essas instâncias por seus construtores. Os serviços guardam dependências; não armazenam rascunhos ou usuário atual em campos compartilhados.

Isso não impede criar outra instância com `new` e não representa uma instância única para todas as JVMs. A demonstração deve mostrar o contexto Spring e a injeção nos controladores, mantendo essa distinção em relação ao Singleton GoF clássico.

## 7. Exclusão lógica

`UsuarioService.desativar()` define `ativo = false`, preserva o registro e impede uma nova autenticação. A lista oferece reativação.

`EventoService.arquivar()` define `ativo = false` e retira o evento das consultas ativas e dos detalhes públicos, preservando o registro. Cancelar apenas muda o estado para `CANCELADO`; arquivar retira da programação ativa. Não há tela de restauração de eventos nesta versão.

## 8. Roteiro de apresentação

1. Entre como universidade e cadastre um centro acadêmico. Depois entre com essa conta.
2. Na tela de cadastrar palestrante, preencha nome e e-mail: a prévia muda. Mostre `PreviasController.usuario()`, `FormularioSubject.alterar()` e `PreviaUsuarioObserver.atualizar()` no Java.
3. Na tela de cadastrar evento, altere título, data, local e vagas: o cartão muda. Mostre o segundo Observer em Java.
4. Salve e edite os dois registros. Mostre os métodos `criarGerenciado()`/`criar()` e `atualizarGerenciado()`/`atualizar()` nos serviços.
5. Mostre a injeção dos serviços nos controladores e o teste de escopo singleton do Spring.
6. Mostre as transições de estado do evento: lotar ao preencher vagas, cancelar e finalizar. Mostre o state da inscrição: cancelar e reativar. Destaque que cada estado concreto decide o que é permitido.
7. Percorra as listas, cadastros, edições e confirmações de exclusão lógica para demonstrar as oito telas.

## 9. Verificação

**Resultado desta entrega:** 19 testes Java e 4 testes da ponte JavaScript passaram. `./mvnw clean package` concluiu e o JAR foi conferido sem as antigas classes de auditoria/métricas.

[ObservadoresFormularioTest](</Users/otavioaugusto/My Projects/projetoReuso/src/test/java/br/edu/projetoreuso/ObservadoresFormularioTest.java>) testa inscrição/notificação/cancelamento, atualização das duas prévias, rascunhos inválidos, imutabilidade dos resultados e isolamento entre formulários.

[RequisitosCrudTest](</Users/otavioaugusto/My Projects/projetoReuso/src/test/java/br/edu/projetoreuso/RequisitosCrudTest.java>) testa as oito telas, CRUD, permissões, CSRF, rollback, beans singleton e as rotas de prévia sem gravação no banco.

[previas-formularios.test.mjs](</Users/otavioaugusto/My Projects/projetoReuso/src/test/js/previas-formularios.test.mjs>) testa somente a ponte do navegador: seleção de campos, envio do token, apresentação como texto, respostas fora de ordem e falha de conexão.

```bash
./mvnw clean package
node --test src/test/js/previas-formularios.test.mjs
```

Os testes Java usam H2 e MockMvc com renderização Thymeleaf. Node é necessário apenas para testar a ponte JavaScript. MySQL e aparência em diferentes dispositivos não foram validados por esses testes. O pacote é `target/projeto-reuso-0.3.0.jar`; o módulo de equipamentos continua fora do projeto.


## 10. Inscrição real do estudante em eventos

A partir da versão 0.3.0, a inscrição real existe na interface e é diferente do registro técnico de um Observer:

- Entre com a conta de estudante, abra **Eventos**, escolha **Ver mais** e use **Inscrever-me**.
- **Minhas inscrições** mostra o registro confirmado e permite **Cancelar inscrição**.
- O centro acadêmico gerencia seus eventos; a universidade gerencia seus centros. Somente estudantes ativos podem se inscrever.
- Uma inscrição ocupa uma vaga. Inscrição duplicada é rejeitada; cancelar libera a vaga e permite nova inscrição, preservando o mesmo registro.
- A reserva usa bloqueio da linha do evento no banco e isolamento READ_COMMITTED para que inscrições simultâneas não ultrapassem a capacidade. O último lugar muda o evento para Lotado; cancelar pode reabri-lo.
- Eventos cancelados, finalizados, arquivados ou já iniciados não aceitam novas inscrições. O organizador não pode reduzir a capacidade abaixo das inscrições confirmadas.

O fluxo é implementado em [InscricaoService](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/inscricao/InscricaoService.java>), [InscricoesController](</Users/otavioaugusto/My Projects/projetoReuso/src/main/java/br/edu/projetoreuso/web/InscricoesController.java>) e [inscricoes/lista.html](</Users/otavioaugusto/My Projects/projetoReuso/src/main/resources/templates/inscricoes/lista.html>). A tabela persistida é `inscricao_evento`, criada no MySQL pela migração `V2__inscricoes_eventos.sql`.

As oito telas CRUD já mapeadas permanecem como base da apresentação; os detalhes com o botão de inscrição e a página Minhas inscrições são fluxos adicionais solicitados.

## 11. Demonstração pronta para uso

O perfil `demo` cria automaticamente quatro contas fictícias e três eventos futuros, ligados ao centro acadêmico e ao palestrante de exemplo. Os dados são temporários. Veja o README para o comando de inicialização.

| Perfil | Login | Senha de demonstração |
|---|---|---|
| Universidade | `universidade@demo.com` | `Demo1234` |
| Centro acadêmico | `centro@demo.com` | `Demo1234` |
| Estudante | `usuario@demo.com` | `Demo1234` |
| Palestrante de exemplo | `palestrante@demo.com` | `Demo1234` |

A universidade já encontra seu centro cadastrado; o centro já encontra um palestrante e três eventos; o estudante já encontra os eventos disponíveis para inscrição.
