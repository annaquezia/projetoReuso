package usuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "palestrante")
@DiscriminatorValue("PALESTRANTE")
public class Palestrante extends Usuario {
    @Column(name = "area_atuacao", length = 120)
    private String areaAtuacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_academico_id")
    private CentroAcademico centroAcademico;

    public String getAreaAtuacao() { return areaAtuacao; }
    public void setAreaAtuacao(String areaAtuacao) { this.areaAtuacao = areaAtuacao; }
    public CentroAcademico getCentroAcademico() { return centroAcademico; }
    public void setCentroAcademico(CentroAcademico centroAcademico) { this.centroAcademico = centroAcademico; }
}
