package br.edu.projetoreuso;

import br.edu.projetoreuso.inscricao.InscricaoEvento;
import evento.Evento;
import evento.EventoStatus;
import inscricao.state.InscricaoAtivaState;
import inscricao.state.InscricaoCanceladaState;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class EstadosTest {
    @Test
    void singletonsDeEstadoSaoCompartilhadosSemCompartilharDadosDoDominio() {
        Evento primeiro = new Evento();
        Evento segundo = new Evento();
        assertThat(primeiro.getEventoState()).isSameAs(evento.state.DisponivelState.getInstance());
        assertThat(segundo.getEventoState()).isSameAs(primeiro.getEventoState());
        primeiro.cancelar();
        assertThat(segundo.getStatus()).isEqualTo(EventoStatus.DISPONIVEL);

        InscricaoEvento primeira = new InscricaoEvento();
        InscricaoEvento segunda = new InscricaoEvento();
        assertThat(primeira.getEstado()).isSameAs(InscricaoAtivaState.getInstance());
        assertThat(segunda.getEstado()).isSameAs(primeira.getEstado());
        primeira.cancelar();
        assertThat(segunda.isAtiva()).isTrue();
    }

    @Test
    void estadosDeEventoControlamTransicoesETerminais() {
        Evento evento = new Evento();
        evento.lotar();
        assertThat(evento.getStatus()).isEqualTo(EventoStatus.LOTADO);
        assertThatThrownBy(evento::lotar).isInstanceOf(IllegalStateException.class);
        evento.reabrir();
        assertThat(evento.getStatus()).isEqualTo(EventoStatus.DISPONIVEL);
        evento.cancelar();
        assertThatThrownBy(evento::reabrir).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(evento::finalizar).isInstanceOf(IllegalStateException.class);

        Evento encerrado = new Evento();
        encerrado.finalizar();
        assertThat(encerrado.getStatus()).isEqualTo(EventoStatus.FINALIZADO);
        assertThatThrownBy(encerrado::cancelar).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(encerrado::lotar).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void estadosDaInscricaoSincronizamAtividadeEDatas() {
        InscricaoEvento inscricao = new InscricaoEvento();
        inscricao.setDataInscricao(LocalDateTime.now().minusDays(1));
        assertThat(inscricao.getEstado()).isInstanceOf(InscricaoAtivaState.class);
        assertThatThrownBy(inscricao::reativar).isInstanceOf(IllegalStateException.class);
        inscricao.cancelar();
        assertThat(inscricao.isAtiva()).isFalse();
        assertThat(inscricao.getEstado()).isInstanceOf(InscricaoCanceladaState.class);
        assertThat(inscricao.getDataCancelamento()).isNotNull();
        assertThatThrownBy(inscricao::cancelar).isInstanceOf(IllegalStateException.class);
        inscricao.reativar();
        assertThat(inscricao.isAtiva()).isTrue();
        assertThat(inscricao.getDataCancelamento()).isNull();
        assertThat(inscricao.getDataInscricao()).isAfter(LocalDateTime.now().minusMinutes(1));
    }
}
