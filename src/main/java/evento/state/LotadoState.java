package evento.state;

import evento.Evento;

public final class LotadoState implements EventoState {
    private static final LotadoState INSTANCE = new LotadoState();
    private final String nome;

    private LotadoState() { this.nome = "Lotado"; }

    public static LotadoState getInstance() { return INSTANCE; }

    @Override
    public void reabrir(Evento evento) {
        evento.setEventoState(DisponivelState.getInstance());
    }

    @Override
    public void lotar(Evento evento) {
        throw new IllegalStateException("O evento já está lotado");
    }

    @Override
    public void cancelar(Evento evento) {
        evento.setEventoState(CanceladoState.getInstance());
    }

    @Override
    public void finalizar(Evento evento) {
        evento.setEventoState(FinalizadoState.getInstance());
    }

    @Override
    public String getNome() {
        return nome;
    }
}
