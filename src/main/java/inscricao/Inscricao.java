package inscricao;

import evento.Evento;
import inscricao.state.InscricaoAtivaState;
import inscricao.state.InscricaoState;
import usuario.Aluno;

import java.time.LocalDateTime;

public class Inscricao {
    private Long id;
    private Aluno aluno;
    private Evento evento;
    private LocalDateTime dataInscricao;
    private InscricaoState inscricaoState = new InscricaoAtivaState();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public InscricaoState getInscricaoState() {
        return inscricaoState;
    }

    public void setInscricaoState(InscricaoState inscricaoState) {
        this.inscricaoState = inscricaoState;
    }

    public void cancelar() {
        inscricaoState.cancelar(this);
    }

    public void reativar() {
        inscricaoState.reativar(this);
        this.dataInscricao = LocalDateTime.now();

    }
}
