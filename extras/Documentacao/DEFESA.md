# Defesa — Conecta Campus

## 1. Visão geral

Equipe: **Laura, Otavio e Anna**. O requisito confirmado é **seis telas distintas de
operações CRUD**, duas por integrante, usando usuários e eventos. Não se exige um
cadastro completo diferente para cada tela. A autoria histórica não foi informada.
Sugestão apenas de estudo: Laura (criar/consultar usuário), Otavio (editar usuário/criar
evento), Anna (consultar/excluir evento). Todos devem conhecer os seis fluxos.

Universidade gerencia centros; centro gerencia palestrantes, eventos e comunicados;
estudante consulta eventos e faz/cancela inscrições.
Stack preservada: Java 17, Spring Boot MVC/Thymeleaf, Security, JPA/Hibernate, Flyway e MySQL.

## 2. Fluxo frontend → backend → banco

Ação no HTML → POST com CSRF → controller com `@Valid` → serviço transacional que
valida perfil/proprietário e regras → repositório JPA → Hibernate → MySQL → commit
→ redirect → consulta e renderização Thymeleaf. Falhas de regra voltam ao formulário;
falta de permissão produz 403; vínculos impedem exclusão física.

Exemplo: `eventos/form.html` → POST `/centro-academico/eventos/novo` →
`EventoGestaoController.criar` → `EventoService.criar` → `preencher` → `EventoRepository.save` → tabela `evento`.

Prévias: o JavaScript envia rascunho JSON; `PreviasController` registra um observador
Java e chama `FormularioSubject.alterar`; o resultado volta como texto para o cartão.
Nenhum rascunho é gravado. A aplicação não tem uma SPA separada.

## 3. Seis telas e localização real

| Tela / operação | Rota | Controller e método | Persistência |
|---|---|---|---|
| Novo usuário / C | `/usuarios/novo` | `UsuariosController.novo/criar` | `UsuarioService.criarGerenciado` → `usuario` e subtipo |
| Lista de usuários / R | `/usuarios` | `UsuariosController.listar` | `UsuarioService.listarGerenciados` → consulta JPA |
| Editar usuário / U | `/usuarios/{id}/editar` | `UsuariosController.editar/atualizar` | `UsuarioService.atualizarGerenciado` → `save` |
| Novo evento / C | `/centro-academico/eventos/novo` | `EventoGestaoController.novo/criar` | `EventoService.criar` → `evento` |
| Lista de eventos / R | `/centro-academico/eventos` | `EventoGestaoController.listar` | `EventoService.listarDoCentro` → consulta JPA |
| Excluir evento / D | `/centro-academico/eventos/{id}/excluir` | `EventoGestaoController.confirmarExclusao/excluir` | `EventoService.excluir` → `delete/flush` |

Templates reais: `usuarios/{novo,lista,editar}.html`, `eventos/{novo,gestao-lista}.html`
e `excluir.html`, todos sob `src/main/resources/templates`. Usuários e eventos também
mantêm as outras operações válidas. Não confundir seis telas com seis recursos completos.
GET de confirmação não altera dados; DELETE é realizado por POST autenticado com CSRF.

`ComunicadoController` → `ComunicadoService` → `ComunicadoRepository` → `comunicado`
permanece porque já havia conteúdo em uso. Não compõe a contagem mínima.
Categorias, espaços e presenças foram retirados após confirmar ausência de dados/vínculos.

## 4. Tabelas e relacionamentos

Oito tabelas: `usuario`, `universidade`, `centro_academico`, `palestrante`, `estudante`,
`evento`, `inscricao_evento` e `comunicado`. Todas possuem PK. `Usuario` usa herança
JOINED: a PK do subtipo também é FK de `usuario`. Centro referencia universidade;
palestrante referencia centro; evento referencia centro/palestrante; inscrição
referencia estudante/evento e tem par único; comunicado referencia evento.

Flyway mantém V1–V5 aplicadas. V4/V5 retiram somente estruturas comprovadamente
vazias; não se altera migração já aplicada. Hibernate valida o esquema.
Mais de dez classes concretas com atributos e métodos atuam nos fluxos: entidades,
`UsuarioService`, `EventoService`, `InscricaoService`, `ComunicadoService`, os dois
Observers e os objetos State. Não se contam arquivos de configuração para inflar a soma.

## 5. Os seis exemplos de padrões

### Singleton 1 — estados de evento

Exemplo principal: `evento/state/DisponivelState`. Construtor privado, classe final,
`private static final INSTANCE` e `getInstance()` garantem uma instância por classe/
carregador de classes. `Evento.restaurarEstado` e transições usam essa instância.
Resolve a criação repetida de comportamento idêntico. O objeto guarda só nome
imutável; recebe o evento por parâmetro e não guarda dados de participante ou evento.
Reuso: eventos distintos compartilham as operações. Sem Singleton, restauração e
transições construiriam novos estados. Alterações de regra ficam na classe do estado.

### Singleton 2 — estados de inscrição

`inscricao/state/InscricaoAtivaState` tem a mesma garantia por construtor privado e
`getInstance`, em outro contexto de domínio. `InscricaoEvento.restaurarEstado` e
`InscricaoCanceladaState.reativar` reutilizam o objeto. Resolve a repetição do
comportamento de inscrições ativas sem compartilhar atividade/datas entre inscrições.
Sem ele, cada carga/reativação criaria novo objeto. `EstadosTest` prova identidade e
isolamento dos contextos; o teste MySQL prova identidade após recarregar a entidade.
Singleton e State colaboram: um controla criação; o outro organiza comportamento.

### Observer 1 — prévia de usuário

`FormularioSubject<DadosUsuarioPrevia>` mantém observadores por `registrarObserver`,
permite `removerObserver` e notifica por `alterar`. `PreviaUsuarioObserver.atualizar`
monta nome, perfil, e-mail e detalhe. Entrada: `PreviasController.usuario`, rotas
`/usuarios/previa` e `/minha-conta/previa`. Resolve apresentação do rascunho sem
persistência/senha. O publicador só conhece `FormularioObserver`, reduzindo acoplamento.
Uma nova reação implementa o contrato; sem ele, o publicador chamaria tipos concretos.

### Observer 2 — prévia de evento

`PreviasController.evento` registra `PreviaEventoObserver` no Subject para
`DadosEventoPrevia`. A rota `/centro-academico/eventos/previa` formata título, agenda,
local e vagas, inclusive rascunhos incompletos. Reutiliza registro/notificação em
um segundo contexto, com dados e comportamento próprios. Alterações de apresentação
ficam no observador; sem o contrato, a coordenação dependeria desse formato concreto.
Subject/Observer nascem por requisição, isolando rascunhos de usuários diferentes.

### State 1 — evento

`Evento` delega `lotar/cancelar/finalizar/reabrir` a `EventoState`. Os objetos
`DisponivelState`, `LotadoState`, `CanceladoState`, `FinalizadoState` decidem ações e
transições. Disponível pode lotar/cancelar/finalizar; lotado pode reabrir/cancelar/finalizar;
terminais recusam transições. O enum persiste e `@PostLoad` restaura o objeto.
Gestão e inscrição reutilizam as regras. Sem State, condicionais se repetiriam nos
serviços. Uma mudança de transição ocorre no estado correspondente.

### State 2 — inscrição

`InscricaoEvento.cancelar/reativar` delegam a `InscricaoState`. Ativa permite cancelar;
cancelada permite reativar; ações repetidas são recusadas. `mudarEstado` sincroniza
atividade e datas; `@PostLoad` restaura a instância compartilhada a partir do booleano.
`InscricaoService` usa o padrão no cancelamento e na reinscrição, preservando o ID.
Reutiliza transições consistentes; sem State, checagens e datas ficariam repetidas nos
comandos. A decisão está no objeto concreto e não em um enum renomeado.

Os serviços Spring continuam com seu escopo normal; os dois exemplos Singleton
GoF acima são explícitos em Java e não dependem de anotações do framework.

## 6. Variabilidade LPS

Núcleo comum: perfis, cadastros, catálogo, comunicados e persistência.
Ponto de variação: oferta de inscrições. Variantes: **catálogo com inscrições**
(padrão) e **somente divulgação**. Seleção na inicialização por
`APP_INSCRICOES_HABILITADAS=true/false` → `VarianteProduto`.
`VarianteProdutoAdvice` leva a seleção ao HTML; `InscricaoService` recusa operações
quando desabilitadas, mesmo com HTTP manual. Registros históricos não são apagados.
Ambas as variantes reutilizam o mesmo núcleo. Testes cobrem as duas seleções.

## 7. Compactação

Extract Method: `UsuarioService.aplicarAtualizacao` centraliza validação, preenchimento
e hash opcional, chamado por `atualizarGerenciado` e `atualizarMinhaConta`. Quatro
famílias de duplicação passam de duas ocorrências para uma. A matrícula ganhou
validação própria; não se afirma redução total de linhas do arquivo.
Original do commit `f57f9c7`, diff, roteiro e instruções: [extras/Compactacao/README.md](../Compactacao/README.md).
[Vídeo revisado local](<../Compactacao/video/Compactacao por Reuso.mp4>).
A equipe dispensou novas gravações/publicações; o vídeo anterior no YouTube foi preservado.
Os formulários de criar/editar reutilizam campos; confirmações compartilham `excluir.html`.

## 8. Roteiro de demonstração

1. Iniciar com MySQL, cinco migrações aplicadas e validação Hibernate.
2. Centro: criar palestrante, consultar e editar nas três telas de usuário; conferir SQL por ID.
3. Criar evento, consultar a lista, editar e abrir a confirmação de exclusão. Confirmar
   somente para dados de teste; conferir SQL após cada alteração. A edição é complementar;
   as três telas de evento contadas são criação, lista e exclusão.
4. Mostrar as prévias Java. Abrir **Mais ações** para ver transições e confirmações.
5. Estudante: inscrever; a última vaga muda para LOTADO; cancelar libera; reinscrever usa o mesmo ID.
6. Mostrar o comunicado já existente no detalhe do evento, sem removê-lo.
7. Localizar os Singletons, States, Observers, seleção da variante e Extract Method.
8. Abrir listas e formulários no celular; conferir botões, campos e menu sem corte lateral.
9. Consultar [extras/Evidencias/README.md](../Evidencias/README.md) e executar os scripts de validação.

## 9. Quinze perguntas para revisão

1. **Como o HTML salva?** POST → controller com `@Valid` → serviço transacional → JPA → MySQL → redirect; exemplo `EventoGestaoController.criar`.
2. **O que faz o JavaScript?** Transporta rascunhos e apresenta texto; a prévia é calculada por Java em `PreviasController`/Observers.
3. **Quais são os Singletons?** `DisponivelState` e `InscricaoAtivaState` exemplificam os dois domínios; construtor privado e `getInstance()` compartilham comportamento imutável.
4. **Por que isso não mistura usuários?** O estado não guarda o contexto; o evento/inscrição é parâmetro da ação. `EstadosTest` prova que mudar um contexto não altera outro.
5. **Onde Observer notifica?** `FormularioSubject.alterar` chama `atualizar` de cada observador registrado; os dois Observers usam dados próprios.
6. **Registrar Observer inscreve aluno?** Não. Inscrição de evento é `InscricaoService.inscrever` e grava `inscricao_evento`.
7. **Por que State não é só enum?** Enum/booleano persiste o valor; objetos concretos executam ou recusam ações por polimorfismo.
8. **Como State volta após reinício?** `@PostLoad` restaura o Singleton correspondente ao valor persistido, em `Evento` e `InscricaoEvento`.
9. **Onde há OO?** Herança JOINED em `Usuario` e subtipos; interfaces e despacho polimórfico nos padrões; entidades encapsulam atributos e comportamento.
10. **Como provar cada CRUD?** SQL pelo ID após C/U/D; `MysqlPersistenciaTest` cobre usuários/eventos e `MysqlComunicadosTest` cobre os comunicados preservados.
11. **Por que não apagar qualquer registro?** Serviços e FKs protegem vínculos; usuários/eventos têm desativação/arquivamento para histórico.
12. **Como evitar exceder vagas?** Bloqueio pessimista em `EventoRepository.findBloqueadoById` e contagem na mesma transação READ_COMMITTED em `InscricaoService`.
13. **Por que separar camadas?** Controller trata HTTP, serviço concentra regras/transação e repositório consulta/grava; os templates comuns reduzem repetição de apresentação.
14. **Qual é a LPS?** `VarianteProduto` seleciona inscrições ligadas/desligadas; catálogo e cadastros são o núcleo reutilizado.
15. **O que foi compactado?** Regras repetidas nas duas edições de usuário foram extraídas para `aplicarAtualizacao`; há original, diff e testes, não minificação.
