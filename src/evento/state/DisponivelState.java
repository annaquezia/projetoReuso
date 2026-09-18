package evento.state;

import evento.Evento;

public class DisponivelState implements EventoState{

    @Override
    public void lotar(Evento evento) {
        evento.setEventoState(new LotadoState());
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
        return "Disponível";
    }
}
