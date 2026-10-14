package certificado;

import evento.Evento;
import usuario.Aluno;

import java.time.LocalDateTime;

public class Certificado {
    private Long id;
    private Aluno aluno;
    private Evento evento;
    private LocalDateTime dataEmissao;
    private String assinaturaReitoria;
    private String caminhoArquivo;

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

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public String getAssinaturaReitoria() {
        return assinaturaReitoria;
    }

    public void setAssinaturaReitoria(String assinaturaReitoria) {
        this.assinaturaReitoria = assinaturaReitoria;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }
}
