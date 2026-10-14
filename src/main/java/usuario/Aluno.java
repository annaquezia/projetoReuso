package usuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name = "estudante")
@DiscriminatorValue("ALUNO")
public class Aluno extends Usuario {
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @NotBlank
    @Column(name = "num_matricula", nullable = false, unique = true, length = 45)
    private String matricula;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String curso;

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
}
