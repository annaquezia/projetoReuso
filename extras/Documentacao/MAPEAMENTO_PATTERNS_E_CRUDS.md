# Mapeamento dos padrões e CRUDs

O mapa completo está em [DEFESA.md](DEFESA.md).

| Padrão | Exemplo 1 | Exemplo 2 | Uso real |
|---|---|---|---|
| Singleton GoF | `DisponivelState.getInstance()` | `InscricaoAtivaState.getInstance()` | Estados imutáveis compartilhados por eventos e inscrições, com construtor privado |
| Observer | `PreviaUsuarioObserver` | `PreviaEventoObserver` | Formulários de usuário/evento → `PreviasController` → `FormularioSubject.alterar` |
| State | `Evento` + `evento/state` | `InscricaoEvento` + `inscricao/state` | Gestão de evento; inscrição, cancelamento e reinscrição |

O Singleton garante uma instância por classe/carregador de classes. Estado do
usuário/evento/inscrição não fica no Singleton. Spring continua gerenciando serviços;
a prova GoF não depende de suas anotações. Singleton e State colaboram, com papéis distintos.

Para **Laura, Otavio e Anna**, são seis telas distintas de operações: criar, consultar
e editar usuário; criar e consultar evento; confirmar/executar exclusão de evento.
As rotas, métodos e templates estão em DEFESA.md. A orientação mais recente da equipe
substitui a interpretação anterior de seis cadastros completos. As demais operações
válidas de usuários/eventos e o comunicado em uso foram preservados.

Consulte [extras/Evidencias/README.md](../Evidencias/README.md) para a validação e
[extras/Compactacao/README.md](../Compactacao/README.md) para o antes/depois.
