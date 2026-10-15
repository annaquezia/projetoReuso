package usuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;


@Entity
@Table(name = "centro_academico")
@DiscriminatorValue("CENTRO_ACADEMICO")
public class CentroAcademico extends Usuario {
    @NotBlank
    @Column(nullable = false, length = 100)
    private String curso;

    @Column(name = "nome_presidente", length = 160)
    private String presidente;

    @Column(name = "vice_presidente", length = 160)
    private String vicePresidente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "universidade_id", nullable = false)
    private Universidade universidade;

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getPresidente() { return presidente; }
    public void setPresidente(String presidente) { this.presidente = presidente; }
    public String getVicePresidente() { return vicePresidente; }
    public void setVicePresidente(String vicePresidente) { this.vicePresidente = vicePresidente; }
    public Universidade getUniversidade() { return universidade; }
    public void setUniversidade(Universidade universidade) { this.universidade = universidade; }
}
