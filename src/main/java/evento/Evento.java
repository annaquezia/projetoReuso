package evento;

import evento.state.CanceladoState;
import evento.state.DisponivelState;
import evento.state.EventoState;
import evento.state.FinalizadoState;
import evento.state.LotadoState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import usuario.CentroAcademico;
import usuario.Palestrante;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "evento")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 180)
    private String nome;

    @NotNull
    @Column(nullable = false)
    private LocalDate data;

    @NotNull
    @Column(nullable = false)
    private LocalTime horario;

    @NotBlank
    @Column(name = "local", nullable = false, length = 255)
    private String local;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Min(1)
    @Column(name = "quantidade_vagas", nullable = false)
    private int quantidadeVagas;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palestrante_id")
    private Palestrante palestrante;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_academico_id", nullable = false)
    private CentroAcademico organizador;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EventoStatus status = EventoStatus.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    @Transient
    private EventoState eventoState;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getQuantidadeVagas() { return quantidadeVagas; }
    public void setQuantidadeVagas(int quantidadeVagas) { this.quantidadeVagas = quantidadeVagas; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public Palestrante getPalestrante() { return palestrante; }
    public void setPalestrante(Palestrante palestrante) { this.palestrante = palestrante; }
    public CentroAcademico getOrganizador() { return organizador; }
    public void setOrganizador(CentroAcademico organizador) { this.organizador = organizador; }
    public EventoStatus getStatus() { return status; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getNomeEstado() {
        return getEventoState().getNome();
    }

    @PostLoad
    private void restaurarEstado() {
        this.eventoState = switch (status) {
            case DISPONIVEL -> DisponivelState.getInstance();
            case LOTADO -> LotadoState.getInstance();
            case CANCELADO -> CanceladoState.getInstance();
            case FINALIZADO -> FinalizadoState.getInstance();
        };
    }

    public EventoState getEventoState() {
        if (eventoState == null) restaurarEstado();
        return eventoState;
    }

    public void setEventoState(EventoState eventoState) {
        EventoState novoEstado = eventoState == null ? DisponivelState.getInstance() : eventoState;
        this.eventoState = novoEstado;
        if (novoEstado instanceof DisponivelState) this.status = EventoStatus.DISPONIVEL;
        else if (novoEstado instanceof LotadoState) this.status = EventoStatus.LOTADO;
        else if (novoEstado instanceof CanceladoState) this.status = EventoStatus.CANCELADO;
        else if (novoEstado instanceof FinalizadoState) this.status = EventoStatus.FINALIZADO;
    }

    public void lotar() {
        getEventoState().lotar(this);
    }

    public void cancelar() {
        getEventoState().cancelar(this);
    }

    public void finalizar() {
        getEventoState().finalizar(this);
    }

    public void reabrir() {
        getEventoState().reabrir(this);
    }
}
