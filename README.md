# Conecta Campus — MVP

Aplicação web Java para administrar **usuários** e **eventos acadêmicos**. O módulo de solicitação de equipamentos não faz parte deste MVP.

## Requisitos

- Java 17 ou superior.
- MySQL 8 para uso normal. Para demonstração local rápida, há um perfil H2 em memória.
- O Maven Wrapper baixa a versão de Maven fixada pelo projeto na primeira execução.

## Executar com MySQL

1. Crie um banco MySQL (ou permita que o usuário configurado o crie) e ajuste as variáveis de ambiente:

   ```sh
   export DB_URL='jdbc:mysql://localhost:3306/projetoreuso?createDatabaseIfNotExist=true&serverTimezone=UTC'
   export DB_USERNAME='root'
   export DB_PASSWORD='sua-senha'
   export APP_BOOTSTRAP_EMAIL='admin@universidade.edu.br'
   export APP_BOOTSTRAP_PASSWORD='uma-senha-com-8-caracteres'
   ```

2. Empacote e inicie:

   ```sh
   ./mvnw package
   java -jar target/projeto-reuso-0.3.0.jar
   ```

   A migração Flyway cria o schema MVP na primeira inicialização. A conta administrativa inicial é do tipo Universidade. O perfil Universidade cria centros acadêmicos; cada Centro Acadêmico pode criar palestrantes e administrar seus eventos. Estudantes podem se cadastrar pela página pública.

3. Abra `http://localhost:8080`.

Para preparar quatro contas de exemplo e três eventos futuros no MySQL, inicie uma vez com as credenciais do banco no ambiente:

```sh
java -jar target/projeto-reuso-0.3.0.jar --spring.profiles.active=seed --app.demo.seed=true
```

O perfil `seed` mantém MySQL, migrações Flyway e validação JPA. Os acessos são os mesmos da tabela abaixo, com senha `Demo1234`. Os registros persistem entre reinícios; contas existentes não têm suas senhas alteradas. Depois da carga, a aplicação pode iniciar normalmente, sem esse perfil.

## Executar uma demonstração sem MySQL

O perfil `demo` usa H2 em memória e cria automaticamente contas, um palestrante e três eventos futuros. Não precisa configurar MySQL nem cadastrar contas manualmente.

```sh
./mvnw package
java -jar target/projeto-reuso-0.3.0.jar --spring.profiles.active=demo --server.port=8081
```

Abra [http://127.0.0.1:8081](http://127.0.0.1:8081). Todos os acessos de demonstração usam a senha **Demo1234**:

| Perfil | E-mail |
|---|---|
| Universidade | `universidade@demo.com` |
| Centro acadêmico | `centro@demo.com` |
| Estudante | `usuario@demo.com` |
| Palestrante de exemplo | `palestrante@demo.com` |

Entre como estudante, abra Eventos, escolha Ver mais e clique em **Inscrever-me**. Use **Minhas inscrições** para acompanhar ou cancelar. A universidade pode administrar o centro cadastrado; o centro pode administrar o palestrante e os três eventos.

Ao encerrar a demonstração, os dados são apagados. A próxima inicialização recria os exemplos. Para persistir alterações entre reinícios, use MySQL. As contas fictícias são criadas no perfil `demo` ou pela carga explícita `seed`; o bootstrap por variáveis de ambiente continua disponível no modo MySQL.

## O que está no MVP

- Login, cadastro de estudante, atualização dos próprios dados e autorização por perfil.
- CRUD lógico de usuários: Universidade administra centros acadêmicos; Centro Acadêmico administra palestrantes; desativação preserva os registros.
- CRUD de eventos administrado pelo Centro Acadêmico, palestrante opcional, listagem pública, detalhes, filtros por status, cancelamento, finalização e arquivamento lógico.
- Padrão State existente para as transições de estado dos eventos.
- Prévias de usuários e eventos calculadas por Observers Java e exibidas enquanto os formulários são preenchidos.
- Inscrição e cancelamento pelo estudante, acompanhamento em Minhas inscrições, controle de vagas e proteção contra inscrição duplicada ou acima da capacidade.
- Migrações versionadas V1 e V2 do schema MySQL; inscrições são persistidas na tabela `inscricao_evento`.

Controle de presença, certificados, integração de calendários, relatórios e equipamentos ficam fora do MVP.


## Padrões aplicados às telas

- **Observer:** `FormularioSubject` notifica `PreviaUsuarioObserver` e `PreviaEventoObserver` no Java. As telas chamam `PreviasController` por HTTP e exibem os textos calculados pelos Observers. O JavaScript apenas envia e apresenta os dados; a prévia não grava no banco.
- Os fluxos de criação e atualização de usuários e eventos são implementados diretamente nos métodos dos serviços, sem uso de Template Method.
- **Singleton:** `UsuarioService` e `EventoService` usam o escopo singleton do Spring e atendem às operações das telas. Não há Singletons GoF explícitos de relógio ou contadores.

Uma conta de centro acadêmico pode apresentar oito telas: listar, cadastrar, editar e confirmar desativação de palestrantes; listar, cadastrar, editar e confirmar arquivamento de eventos.

Veja [MAPEAMENTO_PATTERNS_E_CRUDS.md](./MAPEAMENTO_PATTERNS_E_CRUDS.md) para arquivos, métodos, rotas e roteiro de apresentação.

Para verificar:

```bash
./mvnw package
node --test src/test/js/previas-formularios.test.mjs
```

Os testes Java usam H2 para conferir os fluxos das telas, permissões, CSRF, rollback, escopo dos serviços e Observers das prévias. Os testes JavaScript conferem apenas o transporte e a apresentação, incluindo exclusão de senha do corpo e proteção contra respostas fora de ordem. Node é necessário somente para executar os testes JavaScript, não para abrir a aplicação.


Os testes de inscrições cobrem o fluxo pelas telas, cancelamento/reinscrição, concorrência na última vaga, permissões, CSRF, eventos indisponíveis e alteração de capacidade. Os testes de dados de demonstração verificam contas, vínculos, idempotência e login dos três perfis principais. MySQL permanece uma verificação específica do ambiente de produção.
