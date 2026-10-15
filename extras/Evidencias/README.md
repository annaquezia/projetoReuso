# Evidências da validação — 15/10/2026

Escopo: **Laura, Otavio e Anna**, seis telas de operações CRUD usando usuários e
eventos. Comunicados já utilizados foram preservados; os três cadastros vazios
excedentes foram retirados. Base Git `f57f9c7`, Java 17, Spring Boot 3.5.16, MySQL 26.7.

| Verificação | Resultado observado | Evidência / repetição |
|---|---|---|
| Build e testes gerais | 29 testes Java aprovados; MySQL optativo separado | `sh scripts/validar.sh`; relatórios XML em `target/surefire-reports` |
| Ponte JavaScript | Quatro testes aprovados | `src/test/js/previas-formularios.test.mjs` |
| MySQL real | Quatro testes aprovados; zero falhas/erros/ignorados | `VALIDAR_MYSQL=true ./mvnw '-Dtest=Mysql*Test' test` |
| Total Java efetivo | **33 testes distintos aprovados**, zero falhas/erros/ignorados após as duas execuções | XML por classe em `target/surefire-reports` |
| Usuários/eventos | C/R/U/D nas rotas com consultas SQL por ID após cada escrita | `MysqlPersistenciaTest.crudPelasRotasConfereCadaOperacaoEmSql` |
| Comunicado preservado | C/R/U/D e permissões; conteúdo público e proteção do vínculo com evento | `ComunicadosTest` e `MysqlComunicadosTest` |
| State persistido | Última vaga, cancelamento, reabertura, reinscrição e recarga da entidade aprovados | `MysqlPersistenciaTest.stateRestauraDoMysqlEHistoricoImpedeExclusao` |
| Singleton/Observer | Identidade única e isolamento; registro/remoção/notificação das duas prévias | `EstadosTest`, `ObservadoresFormularioTest`, testes JS |
| LPS | Catálogo com/sem inscrições, proteção no serviço e no HTML | `VarianteCatalogoTest`, `InscricoesTest` |
| Segurança | CSRF, permissões, edição alheia, sessão após alterar e-mail, duplicidade e rollback | `RequisitosCrudTest` e testes MySQL |
| Reinício real | Evento criado/editado por HTTP reapareceu após trocar a JVM; exclusão final confirmada no SQL | `python3 scripts/verificar_reinicio.py --port 8081`; `target/mysql-reinicio.log` |
| Preservação | Hashes de todas as linhas/colunas mantidas das oito tabelas idênticos antes/depois | Snapshot anterior à V5 e comparação após testes; verificador de reinício também compara todo o conteúdo |
| Esquema | Oito tabelas, oito PKs, onze FKs, três UNIQUEs e um CHECK; cinco migrações; Hibernate validou | V1–V5, log da aplicação, `information_schema` |
| Interface | Seis telas conferidas em desktop e celular; listas/cartões, formulários e confirmação com o mesmo padrão | Capturas em `extras/Evidencias/` |
| Compactação | Original e diff conferidos com o Git/código atual | `python3 scripts/compactacao.py --verificar` |
| Vídeo | MP4 revisado somente local; novas gravações/publicações dispensadas pelo usuário; publicação anterior preservada | [Compactação](../Compactacao/README.md) |

Antes desta revisão, o banco tinha **quatro usuários, três eventos, uma inscrição e
um comunicado**. Todos foram preservados. Categorias, espaços e presenças tinham
zero registros e nenhum vínculo; V5 verificou isso novamente antes de remover as
estruturas. V1–V4 já aplicadas não foram alteradas. Não houve seed ou recriação do banco.
Testes excluem somente seus próprios IDs; sequências autoincrementais podem avançar.

## Conferência visual

As seis telas são criar/consultar/editar usuário, criar/consultar evento e confirmar
exclusão de evento. Os GETs de confirmação não modificaram registros. Também foram
verificados comunicado, própria conta, edição de evento e telas públicas.
Conferência em **1280 px** e **390 px**, registrada em
[telas-verificadas.json](telas-verificadas.json). Todas as seis telas
apresentaram largura do documento igual à janela; campos dentro dos limites.
Senha obrigatória na criação e opcional na edição foi conferida no DOM.

| Operação | Captura desktop |
|---|---|
| Criar usuário | [Formulário](usuario-criar-desktop.png) |
| Consultar usuários | [Lista](usuario-consultar-desktop.png) |
| Editar usuário | [Edição](usuario-editar-desktop.png) |
| Criar evento | [Formulário](evento-criar-desktop.png) |
| Consultar eventos | [Lista](evento-consultar-desktop.png) |
| Excluir evento | [Confirmação](evento-excluir-desktop.png) |

No celular: [lista em cartões](evento-consultar-mobile.png),
[formulário](evento-criar-mobile.png) e
[confirmação](evento-excluir-mobile.png).
As [prévias de usuário](previa-usuario.jpg) e
[evento](previa-evento.jpg) foram preenchidas no navegador e aguardadas
até o Java devolver o título informado. Nenhum rascunho foi salvo.
O login do estudante e a consulta da inscrição existente também foram conferidos.

## Limites

- Autoria histórica individual não informada; a divisão de estudo não representa autoria.
- Conferência visual cobre os tamanhos/navegador documentados nas capturas, sem certificar todos os dispositivos.
- Concorrência de vagas foi testada em H2. MySQL teve ciclos sequenciais e bloqueio real, sem ensaio concorrente de carga.
- Credencial fixa removida da configuração atual; histórico preservado e sem rotação nesta tarefa.
- O perfil `demo` usa H2 temporário. As evidências SQL/reinício acima foram obtidas no MySQL normal.
