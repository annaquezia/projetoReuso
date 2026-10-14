package relatorio;

import evento.Evento;
import inscricao.Inscricao;
import presenca.Presenca;

import java.util.List;

public class Relatorio {

    private List<Evento> eventos;
    private List<Inscricao> inscricoes;
    private List<Presenca> presencas;

    public List<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }

    public List<Inscricao> getInscricoes() {
        return inscricoes;
    }

    public void setInscricoes(List<Inscricao> inscricoes) {
        this.inscricoes = inscricoes;
    }

    public List<Presenca> getPresencas() {
        return presencas;
    }

    public void setPresencas(List<Presenca> presencas) {
        this.presencas = presencas;
    }
}
