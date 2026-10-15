# Auditoria acadêmica — 14/10/2026

## Estado inicial, antes das correções

Base Git: `f57f9c7`, árvore limpa. Sistema de eventos acadêmicos em Java 17,
Spring Boot 3.5.16, MVC/Thymeleaf, Security, JPA/Hibernate, MySQL e Flyway.
O navegador recebe HTML do servidor; as prévias usam POST JSON em JavaScript.
O MySQL local respondeu e contém quatro usuários e três eventos.

| REQUISITO | STATUS | EVIDÊNCIA | PROBLEMA | CORREÇÃO |
|---|---|---|---|---|
| Pelo menos dez classes relevantes | ATENDE | Entidades `Usuario`, `Aluno`, `CentroAcademico`, `Universidade`, `Palestrante`, `Evento`, `InscricaoEvento`; serviços `UsuarioService`, `EventoService`, `InscricaoService`; dois Observers com estado e métodos | Classes legadas não devem inflar a contagem | Contar somente participantes dos fluxos reais |
| Dois Singletons no escopo adequado | ATENDE PARCIALMENTE | `UsuarioService` e `EventoService`, `@Service`; teste de identidade no contexto | Documentação mistura a exigência GoF com o escopo Spring | Explicitar o escopo por contexto e verificar compartilhamento entre controladores |
| Dois Observers | ATENDE PARCIALMENTE | `FormularioSubject`, `PreviaUsuarioObserver`, `PreviaEventoObserver`, `PreviasController`; testes existentes | Ainda falta executar a verificação atual | Executar testes de notificação e rotas reais |
| Dois States reais | NÃO ATENDE | `evento/state` usado por `Evento`; `inscricao/state` usa `inscricao.Inscricao` | A tela grava `InscricaoEvento.ativa` diretamente e nunca usa o segundo State | Integrar os estados existentes à entidade persistida |
| Dois CRUDs por integrante | NÃO VERIFICADO | `UsuariosController`, `EventoGestaoController`, oito páginas, dois cadastros completos | Quantidade e nomes da equipe ausentes; oito páginas não equivalem a oito CRUDs | Solicitar integrantes e manter contagem conservadora de dois recursos |
| CRUD no banco real | NÃO VERIFICADO | Serviços e repositórios JPA existem; banco acessível | Ciclos atuais ainda não executados em MySQL; exclusão é lógica | Executar C/R/U/D lógico e consultar SQL após cada operação; demonstrar reinício |
| Três tabelas com integridade | ATENDE PARCIALMENTE | MySQL: `usuario`, `universidade`, `centro_academico`, `estudante`, `palestrante`, `evento`, `inscricao_evento`; V1/V2 | DDL e compatibilidade ainda por validar nesta execução | Conferir PK/FK/UNIQUE/CHECK e iniciar com `ddl-auto=validate` |
| Variabilidade LPS | NÃO ATENDE | Nenhum ponto de seleção funcional identificado | Perfis de banco não demonstram variante funcional do produto | Pequena opção de catálogo com/sem inscrições, preservando o núcleo |
| Compactação por reuso e antes/depois | NÃO ATENDE | Duplicação em `atualizarGerenciado` e `atualizarMinhaConta` | Validação e preenchimento repetidos, sem registro didático | Extract Method com captura do original e diff verificável |
| Repositório Git | ATENDE | `.git`, `git status`, histórico preservado | Nenhum | Manter alterações locais, sem push |
| Código necessário e modular | ATENDE PARCIALMENTE | Controllers/serviços/repositórios/formulários separados | Classes de certificado, presença, QR Code, relatório e localização sem consumidores ativos; campos transientes abandonados | Remover somente após busca de referências |
| Configuração reproduzível | NÃO ATENDE | `application.properties` e README divergentes | Credencial fixa no código; variáveis documentadas ignoradas | Ler usuário/senha do ambiente, sem registrar segredo |
| Validação de entradas | ATENDE PARCIALMENTE | `@Valid`, Bean Validation, permissões e CSRF | Evento sem limites de tamanho; atualização de matrícula pode colidir; atualização da própria sessão deve ser comprovada | Corrigir pontos locais e testar rollback/erros/sessão |
| Execução e regressão | NÃO VERIFICADO | Testes existentes em execução com Java 17 | Nenhuma aprovação antecipada | Empacotar, executar e conferir telas |
| Defesa e vídeo | NÃO ATENDE | README, mapeamento e plano existentes | Plano cita Template Method removido; State de inscrição documentado como ativo; ausência de roteiro verificável de compactação | Atualizar README, criar DEFESA e evidências; vídeo externo fica pendente |

## Prioridade e menor sequência de correções

1. **P0:** corrigir configuração do MySQL e comprovar início com o esquema existente, sem recriar banco.
2. **P1:** integrar State de inscrição ao fluxo persistido; implementar um ponto pequeno de variabilidade; demonstrar os dois CRUDs no MySQL.
3. **P1:** registrar compactação real e corrigir contagem de CRUDs. A quantidade exigida depende dos integrantes.
4. **P2:** retirar legado confirmado sem consumidores; consolidar atualização de usuários; corrigir validações e persistência da autenticação da própria conta.
5. **P2:** executar regressão e gerar mapa de defesa com perguntas, classes e rotas verdadeiras.

Riscos: preservar exclusão lógica e histórico de inscrições; manter bloqueio pessimista e
READ_COMMITTED na reserva de vagas; não mudar V1/V2 já aplicadas; não alterar contas
existentes durante testes. Vídeo e publicação só serão declarados com evidências reais.

## Reavaliação final — escopo esclarecido em 15/10/2026

Equipe: **Laura, Otavio e Anna**. A orientação mais recente confirma seis **telas de
operações CRUD**, não seis cadastros completos. Essa orientação substitui a interpretação
conservadora da matriz inicial. O comunicado já em uso foi preservado.

| REQUISITO | STATUS | EVIDÊNCIA | PROBLEMA | CORREÇÃO |
|---|---|---|---|---|
| Dez classes relevantes | ATENDE | Entidades, serviços, Observers e States concretos em DEFESA.md | Contagem não pode depender de classes artificiais | Somente participantes dos fluxos reais |
| Dois Singletons GoF | ATENDE | `DisponivelState` e `InscricaoAtivaState`; identidade/isolamento em `EstadosTest` | Escopo Spring isoladamente não demonstra GoF | Construtores privados e comportamento imutável compartilhado |
| Dois Observers | ATENDE | Duas prévias Java, Subject, registro/remoção/notificação; Java e JS testados | Nenhum no fluxo verificado | Java calcula; navegador apresenta o rascunho |
| Dois States | ATENDE | Evento e inscrição persistida; transições e restauração no MySQL | Inscrição original era desconectada | Integração ao contexto JPA real |
| Duas telas por integrante | ATENDE | Seis telas mapeadas no README/DEFESA: C/R/U de usuário, C/R/D de evento | Interpretação anterior ampliou para seis recursos completos | Removidos três cadastros vazios; comunicado em uso preservado |
| Persistência e integridade | ATENDE | Oito tabelas; JPA valida; Flyway V1–V5; testes SQL e reinício | Preservar conteúdo real ao simplificar | V5 aborta se houver dados nas estruturas retiradas; hashes dos dados mantidos conferidos |
| Variabilidade LPS | ATENDE | `VarianteProduto`, catálogo com/sem inscrições; serviço e interface protegidos | Antes não havia opção funcional | Um ponto de seleção, núcleo compartilhado |
| Compactação/reuso | ATENDE | Extract Method em `aplicarAtualizacao`; original/diff verificáveis | Atualização duplicada | Duas entradas reutilizam as mesmas regras |
| Git e modularização | ATENDE | Repositório existente e camadas atuais preservados | Nenhum identificado | Alterações locais, sem push/deploy |
| Código necessário | ATENDE | Sem classes dos cadastros retirados nem TODO/FIXME ativos; JAR inspecionado | Escopo excedente e arquivos sem consumidores | Remoção de código, teste e templates substituídos; migrações aplicadas preservadas |
| Front-end consistente | ATENDE | Cabeçalhos, botões, formulários e confirmações comuns; menu contrastante; listas responsivas | Confirmação sem padrão, botão sem estilo e ações comprimidas | Componentes existentes corrigidos e conferidos no navegador |
| Execução/regressão | ATENDE | 33 testes Java e quatro JS; MySQL, reinício e navegador | Limites descritos em extras/Evidencias/README.md | Scripts reproduzíveis e limpeza somente de dados de teste |
| Vídeo de compactação | ATENDE | Original, diff, cinco quadros narrados e MP4 revisado local | Quadro de validação continha o escopo anterior | Evidência local atualizada; usuário dispensou novas gravações/publicações; publicação anterior preservada |

A autoria histórica individual não foi inventada. A divisão em DEFESA.md é somente
uma sugestão de estudo, para que todos expliquem o conjunto.

## Alterações e justificativas

- **Preservados:** usuários, eventos, inscrição real, comunicado já publicado, dados e histórico Git.
- **Removidos:** categorias, espaços e presenças sem dados/vínculos, seus controllers/forms/serviços/
  entidades/repositórios, hub artificial de seis cadastros e testes específicos substituídos.
- **Removidos:** classes abandonadas de certificado, QR Code, relatórios, localização e inscrição antiga;
  repositório de universidade sem consumidores, imports e fragmento de alertas sem uso.
- **Alterados:** listas com ações secundárias em `details`, cartões no celular, navegação sem duplicação
  de Eventos, formulários e confirmações com a mesma hierarquia visual e foco visível.
- **Alterados:** comunicados com templates próprios e nomes de ações claros; reuso de `EventoService`
  para opções do formulário; bloqueio de exclusão de evento com comunicado.
- **Criados:** V5 com verificação prévia de ausência de dados, testes de comunicados e evidências das seis telas.
- **Mantidos:** os seis exemplos GoF, variante LPS, original/diff de compactação e scripts de validação.

A credencial fixa foi retirada da configuração atual. Seu uso temporário para validar
esta revisão foi autorizado explicitamente, sem exibir/gravar a senha. A rotação não
foi realizada; o histórico Git foi preservado.
