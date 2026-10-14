package br.edu.projetoreuso.inscricao;

import evento.Evento;
import usuario.Aluno;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Inscrição persistida usada pelas telas de eventos e Minhas inscrições. */
@Entity
@Table(name = "inscricao_evento", uniqueConstraints = @UniqueConstraint(name = "uk_inscricao_aluno_evento", columnNames = {"aluno_id", "evento_id"}))
public class InscricaoEvento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
    @Column(nullable = false) private boolean ativa = true;
    @Column(name = "data_inscricao", nullable = false) private LocalDateTime dataInscricao;
    @Column(name = "data_cancelamento") private LocalDateTime dataCancelamento;
    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getDataInscricao() { return dataInscricao; }
    public void setDataInscricao(LocalDateTime dataInscricao) { this.dataInscricao = dataInscricao; }
    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; }
}
