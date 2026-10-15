# Escopo final da avaliação

Equipe: **Laura, Otavio e Anna**. São exigidas **seis telas distintas de operações
CRUD**, duas por integrante. Usuários e eventos já oferecem essas telas; não são
necessários seis cadastros completos. A tabela em README.md e DEFESA.md mapeia cada tela.

A stack permanece Java 17, Spring Boot, Thymeleaf, Security, JPA, Flyway e MySQL.
As operações de usuários/eventos foram preservadas, inclusive exclusão protegida,
desativação, arquivamento e inscrição real. Comunicados já tinham conteúdo em uso
e continuam disponíveis. Categorias, espaços e presenças vazios foram retirados.

- Dois Singletons GoF explícitos nos estados imutáveis de evento e inscrição.
- Dois Observers nas prévias Java; dois contextos State nos fluxos persistidos.
- Variabilidade funcional: catálogo com/sem inscrições.
- Compactação: Extract Method em `UsuarioService.aplicarAtualizacao`, original e diff verificáveis.
- Layout compartilhado: títulos, campos, botões, confirmações e navegação; listas em cartões no celular.
- V1–V5 preservam o histórico aplicado; limpeza de estruturas somente após validar ausência de dados.

Execução: [README.md](../../README.md). Mapa e estudo: [DEFESA.md](DEFESA.md).
Matriz final: [AUDITORIA.md](AUDITORIA.md). Testes: [extras/Evidencias/README.md](../Evidencias/README.md).
Compactação/vídeo: [extras/Compactacao/README.md](../Compactacao/README.md). Autoria histórica não presumida.
