package br.edu.projetoreuso.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CadastroAlunoForm {
    @NotBlank @Size(max = 160)
    private String nome;
    @NotBlank @Email @Size(max = 255)
    private String email;
    @NotBlank @Size(min = 8, max = 72)
    private String senha;
    @NotBlank @Size(max = 45)
    private String matricula;
    @NotBlank @Size(max = 100)
    private String curso;
    @NotNull
    private LocalDate dataNascimento;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
}
