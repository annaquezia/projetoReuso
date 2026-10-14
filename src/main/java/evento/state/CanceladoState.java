package evento.state;

import evento.Evento;

public class CanceladoState implements EventoState {
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
        return "Cancelado";
    }
}
