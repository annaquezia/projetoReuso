package evento.state;

import evento.Evento;

public final class CanceladoState implements EventoState {
    private static final CanceladoState INSTANCE = new CanceladoState();
    private final String nome;

    private CanceladoState() { this.nome = "Cancelado"; }

    public static CanceladoState getInstance() { return INSTANCE; }

    @Override
    public void lotar(Evento evento) {
        throw new IllegalStateException("Um evento cancelado não pode ser lotado");
    }

    @Override
    public void cancelar(Evento evento) {
        throw new IllegalStateException("O evento já está cancelado");
    }

    @Override
    public void finalizar(Evento evento) {
        throw new IllegalStateException("Um evento cancelado não pode ser finalizado");
    }

    @Override
    public String getNome() {
        return nome;
    }
}
