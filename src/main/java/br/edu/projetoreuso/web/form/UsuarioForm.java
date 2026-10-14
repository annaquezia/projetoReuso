package br.edu.projetoreuso.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import usuario.Aluno;
import usuario.CentroAcademico;
import usuario.Palestrante;
import usuario.TipoUsuario;
import usuario.Universidade;
import usuario.Usuario;

import java.time.LocalDate;

public class UsuarioForm {
    @NotBlank @Size(max = 160)
    private String nome;
    @NotBlank @Email @Size(max = 255)
    private String email;
    @Size(max = 72)
    private String senha;
    @NotNull
    private TipoUsuario tipoUsuario;
    private LocalDate dataNascimento;
    @Size(max = 45)
    private String matricula;
    @Size(max = 100)
    private String curso;
    @Size(max = 120)
    private String areaAtuacao;
    @Size(max = 160)
    private String presidente;
    @Size(max = 160)
    private String vicePresidente;
    @Size(max = 255)
    private String endereco;

    public static UsuarioForm from(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.nome = usuario.getNome();
        form.email = usuario.getEmail();
        form.tipoUsuario = usuario.getTipoUsuario();
        if (usuario instanceof Aluno aluno) {
            form.dataNascimento = aluno.getDataNascimento();
            form.matricula = aluno.getMatricula();
            form.curso = aluno.getCurso();
        } else if (usuario instanceof Palestrante palestrante) {
            form.areaAtuacao = palestrante.getAreaAtuacao();
        } else if (usuario instanceof CentroAcademico centro) {
            form.curso = centro.getCurso();
            form.presidente = centro.getPresidente();
            form.vicePresidente = centro.getVicePresidente();
        } else if (usuario instanceof Universidade universidade) {
            form.endereco = universidade.getEndereco();
        }
        return form;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getAreaAtuacao() { return areaAtuacao; }
    public void setAreaAtuacao(String areaAtuacao) { this.areaAtuacao = areaAtuacao; }
    public String getPresidente() { return presidente; }
    public void setPresidente(String presidente) { this.presidente = presidente; }
    public String getVicePresidente() { return vicePresidente; }
    public void setVicePresidente(String vicePresidente) { this.vicePresidente = vicePresidente; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
