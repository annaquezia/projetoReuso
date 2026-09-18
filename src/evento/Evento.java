package evento;

import evento.state.DisponivelState;
import evento.state.EventoState;
import localizacao.Localizacao;
import usuario.CentroAcademico;
import usuario.Palestrante;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evento {

    private Long id;
    private String nome;
    private LocalDate data;
    private LocalTime horario;
    private String local;
    private String descricao;
    private int quantidadeVagas;
    private Palestrante palestrante;
    private CentroAcademico organizador;
    private Localizacao localizacao;
    private EventoState eventoState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getQuantidadeVagas() {
        return quantidadeVagas;
    }

    public void setQuantidadeVagas(int quantidadeVagas) {
        this.quantidadeVagas = quantidadeVagas;
    }

    public Palestrante getPalestrante() {
        return palestrante;
    }

    public void setPalestrante(Palestrante palestrante) {
        this.palestrante = palestrante;
    }

    public CentroAcademico getOrganizador() {
        return organizador;
    }

    public void setOrganizador(CentroAcademico organizador) {
        this.organizador = organizador;
    }

    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Localizacao localizacao) {
        this.localizacao = localizacao;
    }

    public EventoState getEventoState() {
        return eventoState;
    }

    public void setEventoState(EventoState eventoState) {
        if (this.eventoState == null) {
            this.eventoState = new DisponivelState();
        } else {
            this.eventoState = eventoState;
        }
    }

    public void lotar() {
        eventoState.lotar(this);
    }

    public void cancelar() {
        eventoState.cancelar(this);
    }

    public void finalizar() {
        eventoState.finalizar(this);
    }


}
