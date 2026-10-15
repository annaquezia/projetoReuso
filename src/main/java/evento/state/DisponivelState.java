package evento.state;

import evento.Evento;

public final class DisponivelState implements EventoState {
    private static final DisponivelState INSTANCE = new DisponivelState();
    private final String nome;

    private DisponivelState() { this.nome = "Disponível"; }

    public static DisponivelState getInstance() { return INSTANCE; }


    @Override
    public void lotar(Evento evento) {
        evento.setEventoState(LotadoState.getInstance());
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
