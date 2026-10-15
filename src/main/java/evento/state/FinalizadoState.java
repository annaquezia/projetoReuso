package evento.state;

import evento.Evento;

public final class FinalizadoState implements EventoState {
    private static final FinalizadoState INSTANCE = new FinalizadoState();
    private final String nome;

    private FinalizadoState() { this.nome = "Finalizado"; }

    public static FinalizadoState getInstance() { return INSTANCE; }

    @Override
    public void lotar(Evento evento) {
        throw new IllegalStateException("Um evento finalizado não pode ser lotado");
    }

    @Override
    public void cancelar(Evento evento) {
        throw new IllegalStateException("Um evento finalizado não pode ser cancelado");
    }

    @Override
    public void finalizar(Evento evento) {
        throw new IllegalStateException("O evento já está finalizado");
    }

    @Override
    public String getNome() {
        return nome;
    }
}
