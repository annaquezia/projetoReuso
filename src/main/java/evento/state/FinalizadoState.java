package evento.state;

import evento.Evento;

public class FinalizadoState implements EventoState {
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
        return "Finalizado";
    }
}
