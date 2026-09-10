package presenca;

import inscricao.Inscricao;
import usuario.CentroAcademico;

import java.time.LocalDateTime;

public class Presenca {
    private Long id;
    private Inscricao inscricao;
    private LocalDateTime dataValidacao;
    private CentroAcademico validadoPor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public void setInscricao(Inscricao inscricao) {
        this.inscricao = inscricao;
    }

    public LocalDateTime getDataValidacao() {
        return dataValidacao;
    }

    public void setDataValidacao(LocalDateTime dataValidacao) {
        this.dataValidacao = dataValidacao;
    }

    public CentroAcademico getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(CentroAcademico validadoPor) {
        this.validadoPor = validadoPor;
    }
}
