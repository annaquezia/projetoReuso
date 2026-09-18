package evento.state;

import evento.Evento;

public interface EventoState {

    void lotar(Evento evento);
    void cancelar (Evento evento);
    void finalizar (Evento evento);
    String getNome();
}
