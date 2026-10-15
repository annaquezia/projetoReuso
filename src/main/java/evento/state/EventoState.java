package evento.state;

import evento.Evento;

public interface EventoState {

    void lotar(Evento evento);
    void cancelar (Evento evento);
    void finalizar (Evento evento);
    default void reabrir(Evento evento) {
        throw new IllegalStateException("Não é possível reabrir um evento no estado " + getNome() + ".");
    }
    String getNome();
}
