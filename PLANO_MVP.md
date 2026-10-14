# Plano do projeto — MVP de usuários e eventos

Atualizado em **06/10/2026**, versão **0.3.0**.

## Objetivo e escopo

Aplicação web Java empacotável com dois recursos CRUD, Usuário e Evento, e oito telas: listar, cadastrar, editar e confirmar exclusão lógica de cada recurso. A interface segue a identidade do Figma “Medição e Análise”, com vinho `#8A0538`, rosa `#FF0040`, roxo `#9654FF` e navegação por perfil.

Universidade administra seus centros acadêmicos; centro acadêmico administra seus palestrantes e eventos. Estudantes têm cadastro público e os perfis podem editar seus próprios dados. Equipamentos, presença, QR Code, certificados, relatórios e notificações externas ficam fora do MVP.

## Fase 1 — Fundação da aplicação

- Java 17, Spring Boot, MVC, Thymeleaf e Maven Wrapper.
- Spring Security para autenticação, autorização e CSRF.
- JPA, MySQL e migração Flyway; H2 em memória para demonstração e testes.
- Layout compartilhado, cabeçalho público e menu por perfil.
- Serviços de usuários e eventos com escopo singleton do Spring, injetados nos controladores.

O modelo Workbench `Untitled.mwb` e o Java tinham relações diferentes para organização de eventos. A implementação adota o centro acadêmico como organizador e a universidade como responsável pelos centros, seguindo os fluxos das telas. A capacidade total do evento é explícita. O schema versionado não inclui equipamentos nem os módulos fora do MVP.

## Fase 2 — Quatro telas de usuários

1. Listar usuários gerenciados (`/usuarios`).
2. Cadastrar (`/usuarios/novo`).
3. Editar (`/usuarios/{id}/editar`).
4. Confirmar desativação (`/usuarios/{id}/desativar`).

- Validar permissão, e-mail único e senha com hash.
- Usar Template Method de criação e atualização para as regras compartilhadas, especializando o recurso Usuário.
- Usar Observer na prévia do formulário, para conferir nome, perfil e e-mail enquanto os campos são preenchidos.
- Preservar os registros ao desativar e permitir reativação pela listagem.

## Fase 3 — Quatro telas de eventos e entrega

1. Listar eventos do centro (`/centro-academico/eventos`).
2. Cadastrar (`/centro-academico/eventos/novo`).
3. Editar (`/centro-academico/eventos/{id}/editar`).
4. Confirmar arquivamento (`/centro-academico/eventos/{id}/arquivar`).

- Verificar organizador, data, capacidade e palestrante vinculado ao centro.
- Especializar os mesmos Template Methods para criação e atualização de eventos.
- Usar o segundo Observer na prévia do evento, acompanhando título, descrição, data, horário, local e vagas.
- Manter State para disponível, lotado, cancelado e finalizado.
- Arquivar com `ativo = false`, preservando o registro e retirando-o das consultas ativas.
- Conferir as oito telas, operações e permissões e gerar `target/projeto-reuso-0.3.0.jar`.

## Estado e verificação

As oito telas e os padrões descritos estão no código. A camada adicional de auditoria/métricas e os Singletons artificiais foram removidos. Os Observers atuam nas prévias dos formulários, em Java, por meio de `FormularioSubject` e `PreviasController`. JavaScript apenas envia os campos e apresenta o resultado. Template Method e os serviços gerenciados pelo Spring também ficam no Java.

A verificação automatizada usa H2 e MockMvc, além de testes Java dos Observers e testes JavaScript da ponte HTTP. A execução com MySQL e a aparência em dispositivos diferentes precisam ser conferidas no ambiente de uso. No perfil `demo`, os dados desaparecem ao encerrar a aplicação.

```bash
./mvnw package
node --test src/test/js/previas-formularios.test.mjs
```

Veja [README.md](./README.md) para iniciar a aplicação e [MAPEAMENTO_PATTERNS_E_CRUDS.md](./MAPEAMENTO_PATTERNS_E_CRUDS.md) para o roteiro de apresentação.

## Referência visual

[Figma — Medição e Análise](https://www.figma.com/design/Us6K7J8CTQWdox5574sj2O/Medi%C3%A7%C3%A3o-e-An%C3%A1lise?node-id=0-1).


## Complemento solicitado — inscrições e demonstração pronta

- Inscrição real do estudante nos detalhes do evento, listagem em Minhas inscrições e cancelamento.
- Reserva de vagas com bloqueio da linha do evento, unicidade por estudante/evento e atualização de lotação.
- Verificação de perfil, propriedade da inscrição, CSRF, disponibilidade do evento e capacidade mínima ao editar.
- Migração V2 com a tabela `inscricao_evento`.
- Perfil demo com universidade, centro acadêmico, estudante e palestrante prontos, além de três eventos futuros.
- As oito telas CRUD continuam mapeadas; a inscrição é um fluxo adicional autorizado pelo usuário.
