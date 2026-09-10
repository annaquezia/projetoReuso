package usuario;

import java.util.List;

public class CentroAcademico extends Usuario {
    private String curso;
    private String presidente;
    private String vicePresidente;
    private List<String> integrantes;

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getPresidente() {
        return presidente;
    }

    public void setPresidente(String presidente) {
        this.presidente = presidente;
    }

    public String getVicePresidente() {
        return vicePresidente;
    }

    public void setVicePresidente(String vicePresidente) {
        this.vicePresidente = vicePresidente;
    }

    public List<String> getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(List<String> integrantes) {
        this.integrantes = integrantes;
    }
}
