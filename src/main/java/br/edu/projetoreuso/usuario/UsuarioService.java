package br.edu.projetoreuso.usuario;

import br.edu.projetoreuso.web.form.CadastroAlunoForm;
import br.edu.projetoreuso.web.form.UsuarioForm;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import usuario.Aluno;
import usuario.CentroAcademico;
import usuario.Palestrante;
import usuario.TipoUsuario;
import usuario.Universidade;
import usuario.Usuario;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final CentroAcademicoRepository centros;
    private final PalestranteRepository palestrantes;
    private final AlunoRepository alunos;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, CentroAcademicoRepository centros,
                          PalestranteRepository palestrantes, AlunoRepository alunos,
                          PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.centros = centros;
        this.palestrantes = palestrantes;
        this.alunos = alunos;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Aluno cadastrarAluno(CadastroAlunoForm form) {
        if (usuarios.existsByEmailIgnoreCase(form.getEmail()))
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        if (alunos.existsByMatriculaIgnoreCase(form.getMatricula()))
            throw new IllegalArgumentException("Esta matrícula já está cadastrada.");

        Aluno aluno = new Aluno();
        aluno.setNome(form.getNome().trim());
        aluno.setEmail(form.getEmail());
        aluno.setSenha(passwordEncoder.encode(form.getSenha()));
        aluno.setMatricula(form.getMatricula().trim());
        aluno.setCurso(form.getCurso().trim());
        aluno.setDataNascimento(form.getDataNascimento());
        Aluno salvo = alunos.save(aluno);

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarGerenciados(String emailAtor) {
        Usuario ator = usuarioPorEmail(emailAtor);
        if (ator instanceof Universidade universidade)
            return centros.findAllByUniversidade_IdOrderByNome(universidade.getId()).stream().map(Usuario.class::cast).toList();
        if (ator instanceof CentroAcademico centro)
            return palestrantes.findAllByCentroAcademico_IdOrderByNome(centro.getId()).stream().map(Usuario.class::cast).toList();
        throw new AccessDeniedException("Este perfil não administra usuários.");
    }

    @Transactional(readOnly = true)
    public List<TipoUsuario> tiposCriaveis(String emailAtor) {
        Usuario ator = usuarioPorEmail(emailAtor);
        if (ator instanceof Universidade) return List.of(TipoUsuario.CENTRO_ACADEMICO);
        if (ator instanceof CentroAcademico) return List.of(TipoUsuario.PALESTRANTE);
        throw new AccessDeniedException("Este perfil não pode criar usuários.");
    }

    @Transactional
    public Usuario criarGerenciado(String emailAtor, UsuarioForm form) {
        Usuario ator = usuarioPorEmail(emailAtor);
        if (!tiposCriaveis(emailAtor).contains(form.getTipoUsuario()))
            throw new AccessDeniedException("Este perfil não pode criar esse tipo de usuário.");
        if (!StringUtils.hasText(form.getSenha()) || form.getSenha().length() < 8)
            throw new IllegalArgumentException("A senha precisa ter pelo menos 8 caracteres.");
        if (usuarios.existsByEmailIgnoreCase(form.getEmail()))
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");

        Usuario novo;
        if (ator instanceof Universidade universidade) {
            CentroAcademico centro = new CentroAcademico();
            centro.setUniversidade(universidade);
            preencherCentro(centro, form);
            novo = centro;
        } else if (ator instanceof CentroAcademico centro) {
            Palestrante palestrante = new Palestrante();
            palestrante.setCentroAcademico(centro);
            palestrante.setAreaAtuacao(form.getAreaAtuacao());
            preencherBase(palestrante, form);
            novo = palestrante;
        } else {
            throw new AccessDeniedException("Este perfil não pode criar usuários.");
        }
        novo.setSenha(passwordEncoder.encode(form.getSenha()));
        return usuarios.save(novo);
    }

    @Transactional(readOnly = true)
    public Usuario buscarGerenciado(String emailAtor, Long id) {
        Usuario ator = usuarioPorEmail(emailAtor);
        Usuario alvo = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        garantirPermissao(ator, alvo);
        return alvo;
    }

    @Transactional
    public Usuario atualizarGerenciado(String emailAtor, Long id, UsuarioForm form) {
        Usuario alvo = buscarGerenciado(emailAtor, id);
        if (usuarios.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), id))
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        if (form.getTipoUsuario() != alvo.getTipoUsuario())
            throw new IllegalArgumentException("O tipo do usuário não pode ser alterado.");
        if (StringUtils.hasText(form.getSenha()) && form.getSenha().length() < 8)
            throw new IllegalArgumentException("A senha precisa ter pelo menos 8 caracteres.");
        preencherBase(alvo, form);
        preencherPerfil(alvo, form);
        if (StringUtils.hasText(form.getSenha())) alvo.setSenha(passwordEncoder.encode(form.getSenha()));
        return usuarios.save(alvo);
    }

    @Transactional
    public boolean alternarAtivo(String emailAtor, Long id) {
        Usuario ator = usuarioPorEmail(emailAtor);
        Usuario alvo = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        garantirPermissao(ator, alvo);
        alvo.setAtivo(!alvo.isAtivo());
        usuarios.save(alvo);

        return alvo.isAtivo();
    }

    @Transactional(readOnly = true)
    public UsuarioForm formularioDaMinhaConta(String email) {
        return UsuarioForm.from(usuarioPorEmail(email));
    }

    @Transactional(readOnly = true)
    public Usuario buscarAtual(String email) {
        return usuarioPorEmail(email);
    }

    @Transactional(readOnly = true)
    public UsuarioForm formularioGerenciado(String emailAtor, Long id) {
        return UsuarioForm.from(buscarGerenciado(emailAtor, id));
    }

    @Transactional
    public void atualizarMinhaConta(String emailAtor, UsuarioForm form) {
        Usuario alvo = usuarioPorEmail(emailAtor);
        if (usuarios.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), alvo.getId()))
            throw new IllegalArgumentException("Já existe uma conta com este e-mail.");
        if (form.getTipoUsuario() != alvo.getTipoUsuario())
            throw new IllegalArgumentException("O tipo do usuário não pode ser alterado.");
        preencherBase(alvo, form);
        preencherPerfil(alvo, form);
        if (StringUtils.hasText(form.getSenha())) {
            if (form.getSenha().length() < 8) throw new IllegalArgumentException("A senha precisa ter pelo menos 8 caracteres.");
            alvo.setSenha(passwordEncoder.encode(form.getSenha()));
        }
        usuarios.save(alvo);

    }

    private Usuario usuarioPorEmail(String email) {
        return usuarios.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    private void garantirPermissao(Usuario ator, Usuario alvo) {
        if (ator instanceof Universidade universidade && alvo instanceof CentroAcademico centro
                && centro.getUniversidade().getId().equals(universidade.getId())) return;
        if (ator instanceof CentroAcademico centro && alvo instanceof Palestrante palestrante
                && palestrante.getCentroAcademico() != null
                && palestrante.getCentroAcademico().getId().equals(centro.getId())) return;
        throw new AccessDeniedException("Você não pode alterar esse usuário.");
    }

    private void preencherBase(Usuario usuario, UsuarioForm form) {
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail());
    }

    private void preencherPerfil(Usuario usuario, UsuarioForm form) {
        if (usuario instanceof CentroAcademico centro) preencherCentro(centro, form);
        if (usuario instanceof Palestrante palestrante) palestrante.setAreaAtuacao(form.getAreaAtuacao());
        if (usuario instanceof Universidade universidade) universidade.setEndereco(form.getEndereco());
        if (usuario instanceof Aluno aluno) {
            aluno.setCurso(form.getCurso());
            aluno.setDataNascimento(form.getDataNascimento());
            if (StringUtils.hasText(form.getMatricula())) aluno.setMatricula(form.getMatricula());
        }
    }

    private void preencherCentro(CentroAcademico centro, UsuarioForm form) {
        if (!StringUtils.hasText(form.getCurso())) throw new IllegalArgumentException("Informe o curso do centro acadêmico.");
        centro.setCurso(form.getCurso().trim());
        centro.setPresidente(form.getPresidente());
        centro.setVicePresidente(form.getVicePresidente());
        preencherBase(centro, form);
    }


    @Transactional
    public void desativar(String emailAtor, Long id) {
        Usuario alvo = buscarGerenciado(emailAtor, id);
        if (!alvo.isAtivo()) throw new IllegalStateException("O usuário já está desativado.");
        alvo.setAtivo(false);
        usuarios.save(alvo);

    }

}
