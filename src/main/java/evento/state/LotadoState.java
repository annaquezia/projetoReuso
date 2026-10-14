package evento.state;

import evento.Evento;

public class LotadoState implements EventoState {
    @Override
    public void lotar(Evento evento) {
        throw new IllegalStateException("O evento já está lotado");
    }

    @Override
    public void cancelar(Evento evento) {
        evento.setEventoState(new CanceladoState());
    }

    @Override
    public void finalizar(Evento evento) {
        evento.setEventoState(new FinalizadoState());
    }

    @Override
    public String getNome() {
        return "Lotado";
    }
}
