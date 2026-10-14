package relatorio;

import evento.state.EventoState;

import java.time.LocalDate;

public class FiltroRelatorio {
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private Long usuarioId;
    private EventoState eventoState;

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public EventoState getEventoState() {
        return eventoState;
    }

    public void setEventoState(EventoState eventoState) {
        this.eventoState = eventoState;
    }
}
