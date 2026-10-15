# Empacotamento e execução na máquina do professor

## O que é entregue

`extras/Entrega/Projeto Reuso.zip` contém o código-fonte, Maven Wrapper, testes,
um JAR executável com as dependências Java, iniciadores para macOS/Linux e
Windows e os materiais acadêmicos dentro de `extras`. `MANIFESTO.json` registra
o SHA-256 de cada arquivo; o ZIP tem seu próprio arquivo `.sha256` ao lado.

O ZIP não leva o banco do aluno, senha do MySQL, arquivos `.env`, configuração
da IDE ou histórico `.git`. O repositório Git original é preservado localmente;
a cópia anterior de `UsuarioService` e seu diff seguem em `extras/Compactacao`.
O verificador usa Git no repositório original e o SHA-256 registrado quando
executado na entrega extraída, que não contém `.git`.

## Sim, o professor pode executar

O mesmo JAR pode ser executado em Windows, Linux ou macOS com **Java 17** e
um servidor **MySQL 8.0.16 ou superior da série 8.x**, ou outra versão validada
pela equipe. O mínimo 8.0.16 é necessário para aplicar as restrições CHECK.
Não é um instalador: Java e MySQL são pré-requisitos externos. Ele não precisa
instalar Maven, Node, Python ou uma IDE para **executar** o JAR recebido.

Java 17/JDK, acesso à internet e Maven Wrapper são usados para **compilar** o
código. Node é usado somente nos testes JavaScript e Python 3 no empacotador e
nos verificadores auxiliares. O Maven baixa suas dependências na primeira compilação;
a execução do JAR e os ícones/fontes da interface não dependem de CDN.
Imagens externas informadas no cadastro de eventos dependem de seus respectivos links.

A plataforma testada localmente é macOS com Java 17 e MySQL; o iniciador PowerShell
é fornecido, mas precisa de uma conferência no Windows antes de afirmar teste nativo.

## Gerar o ZIP, passo a passo

Na raiz do projeto:

1. Selecione um JDK 17 e confira com `java -version`. O `JAVA_HOME`, se definido,
   deve apontar para esse JDK. Não copie caminhos de outra máquina.
2. Execute `bash scripts/validar.sh`. São executados os testes Java e JavaScript.
   No Windows, use `./mvnw.cmd clean package` e
   `node --test src/test/js/previas-formularios.test.mjs`.
3. Para validar o banco local, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`
   apenas no ambiente e execute `bash scripts/validar.sh --mysql`.
   Nunca coloque uma senha real na documentação ou no código-fonte.
4. Execute `python3 scripts/empacotar.py`. No Windows, pode ser `py -3 scripts/empacotar.py`.
   O script compila/testa com Maven, confere os recursos dentro do JAR e produz o ZIP.
   Depois de uma compilação já conferida, `--sem-compilar` evita recompilar;
   o script recusa recursos desatualizados.
5. Entregue o ZIP e seu `.sha256`, ambos em `extras/Entrega`.
   Não compacte a pasta do projeto inteira manualmente: ela contém o Git e arquivos locais.

## Professor: primeira execução com MySQL

1. Instale Java 17 e MySQL compatível e inicie o servidor MySQL.
2. Extraia o ZIP e abra um terminal na pasta extraída `Projeto Reuso`.
3. Use um banco separado para a avaliação. Os iniciadores usam, por padrão,
   `reuso_avaliacao` em `localhost:3306`. O usuário informado precisa poder criar
   esse banco; alternativamente, crie-o antes com uma conta administrativa e use
   um usuário com permissões de migração e leitura/gravação somente nesse banco.
4. Execute uma vez com a carga opcional de avaliação:

   macOS/Linux:
   ```sh
   bash scripts/iniciar.sh --seed
   ```

   Windows/PowerShell:
   ```powershell
   ./scripts/iniciar.ps1 -Seed
   ```

5. Informe **o usuário e a senha do MySQL do professor** quando o terminal pedir.
   A senha fica oculta durante a digitação e é passada ao processo Java pelo ambiente.
   Os iniciadores não a gravam. O Flyway cria as tabelas e a carga cria quatro
   contas fictícias e três eventos dentro do MySQL real.
6. Abra [http://127.0.0.1:8080](http://127.0.0.1:8080). Entre como centro com
   `centro@demo.com` / `Demo1234` para gerenciar usuários/eventos/comunicados.
   Para gerenciar centros, use `universidade@demo.com` / `Demo1234`.
7. Encerre com `Ctrl+C`. Depois, execute o iniciador **sem** `--seed` / `-Seed`.
   Os dados continuarão no MySQL. A carga é idempotente e não muda senhas existentes.

`Demo1234` é senha de **contas fictícias da aplicação**, não a senha do MySQL.
O professor **não precisa conhecer a senha do banco do aluno**. Ele usa suas próprias
credenciais. O JAR lê `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` na inicialização.
Se essas variáveis já existirem no ambiente, os iniciadores as respeitam.
Não há leitura automática de `.env`.

Para servidor/porta/banco diferentes, defina `DB_URL` antes de iniciar:

```sh
export DB_URL='jdbc:mysql://localhost:3306/meu_banco?createDatabaseIfNotExist=true&serverTimezone=UTC'
export PORT=8081
bash scripts/iniciar.sh --seed
```

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/meu_banco?createDatabaseIfNotExist=true&serverTimezone=UTC'
$env:PORT = '8081'
./scripts/iniciar.ps1 -Seed
```

Se o PowerShell da instituição bloquear scripts, mantenha a política local e
execute o JAR diretamente no terminal com as variáveis de ambiente configuradas:
`java -jar projeto-reuso.jar --spring.profiles.active=seed --app.demo.seed=true --server.address=127.0.0.1`.
O iniciador é apenas uma conveniência; o programa é o JAR.

Em um banco vazio sem carga fictícia, configure `APP_BOOTSTRAP_EMAIL` e
`APP_BOOTSTRAP_PASSWORD` (mínimo oito caracteres) para criar a primeira universidade.
O cadastro gerenciado das demais contas pode então ser demonstrado pela própria interface.

## Sem MySQL, somente para conhecer a interface

`bash scripts/iniciar.sh --demo` ou `./scripts/iniciar.ps1 -Demo` abre o modo H2
em memória. Ele é temporário e não comprova o requisito de persistência MySQL.
A demonstração da avaliação deve usar o fluxo MySQL descrito acima.

## Onde demonstrar reuso

Não há uma tela artificial chamada “Reutilização”. Demonstre o reuso nas telas reais:

| Telas | Elemento compartilhado | Onde explicar |
|---|---|---|
| Novo usuário / Editar usuário | Mesmo fragmento de campos e prévia Observer Java | `templates/usuarios/form.html`, `patterns/observer/PreviaUsuarioObserver.java` |
| Editar usuário / Minha conta | Mesmo método de validação e atualização, preservando autorização própria de cada entrada | `UsuarioService.aplicarAtualizacao` — exemplo principal de Extract Method |
| Novo evento / Editar evento | Mesmo fragmento de formulário e prévia Observer Java | `templates/eventos/form.html`, `patterns/observer/PreviaEventoObserver.java` |
| Listas e confirmações | Navegação, ícones, modal de confirmação e toasts compartilhados | `templates/fragments/layout.html`, `static/js/interface.js` |

Para a compactação, comece em **Editar usuário** (`/usuarios/{id}/editar`) e
compare com **Minha conta** (`/minha-conta`). Mostre que as duas entradas chamam
`aplicarAtualizacao`; depois abra o original e o diff em `extras/Compactacao`.
As seis telas combinadas para a avaliação continuam mapeadas no README e na defesa.
A exclusão tem URL própria para demonstração e abre também em modal nas listagens.

## Problemas comuns

- **Access denied:** usuário/senha/permissão são do MySQL local; confira-os no servidor do professor.
- **Communications link failure:** o MySQL não está acessível no host/porta de `DB_URL`.
- **Port 8080 already in use:** escolha outra porta com `PORT`, como no exemplo.
- **Java incompatível:** confira `JAVA_HOME` e `java -version` e use Java 17.
- **Sem conta para entrar:** em banco vazio, execute com `--seed` / `-Seed` ou configure o bootstrap.

Referências: [Bootstrap Icons](https://icons.getbootstrap.com/) (SVG local e licença MIT)
e [CHECK no MySQL](https://dev.mysql.com/doc/refman/8.0/en/create-table-check-constraints.html).
