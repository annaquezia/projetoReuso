package br.edu.projetoreuso.config;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import evento.Evento;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import usuario.*;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados fictícios para demonstração em memória ou carga explícita no MySQL. */
@Component
@Profile({"demo", "seed"})
@ConditionalOnProperty(name = "app.demo.seed", havingValue = "true")
public class DadosDemonstracao implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final PasswordEncoder senhas;
    public DadosDemonstracao(UsuarioRepository usuarios, EventoRepository eventos, PasswordEncoder senhas) {
        this.usuarios = usuarios; this.eventos = eventos; this.senhas = senhas;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Universidade universidade = (Universidade) usuarios.findByEmailIgnoreCase("universidade@demo.com")
                .orElseGet(() -> {
                    Universidade novo = new Universidade();
                    base(novo, "Universidade de demonstração", "universidade@demo.com");
                    novo.setEndereco("Campus universitário — Curitiba");
                    return usuarios.save(novo);
                });
        CentroAcademico centro = (CentroAcademico) usuarios.findByEmailIgnoreCase("centro@demo.com")
                .orElseGet(() -> {
                    CentroAcademico novo = new CentroAcademico();
                    base(novo, "Centro Acadêmico de Engenharia", "centro@demo.com");
                    novo.setCurso("Engenharia de Software"); novo.setPresidente("Ana Silva");
                    novo.setVicePresidente("Pedro Santos"); novo.setUniversidade(universidade);
                    return usuarios.save(novo);
                });
        usuarios.findByEmailIgnoreCase("usuario@demo.com").orElseGet(() -> {
            Aluno novo = new Aluno();
            base(novo, "Estudante de demonstração", "usuario@demo.com");
            novo.setMatricula("DEMO-2026001"); novo.setCurso("Engenharia de Software");
            novo.setDataNascimento(LocalDate.of(2004, 4, 12));
            return usuarios.save(novo);
        });
        Palestrante palestrante = (Palestrante) usuarios.findByEmailIgnoreCase("palestrante@demo.com")
                .orElseGet(() -> {
                    Palestrante novo = new Palestrante();
                    base(novo, "Marina Costa", "palestrante@demo.com");
                    novo.setAreaAtuacao("Java e arquitetura de software"); novo.setCentroAcademico(centro);
                    return usuarios.save(novo);
                });
        evento("Design patterns na prática com Java", "Uma oficina para experimentar padrões de projeto em aplicações Java.", "Laboratório de informática", 7, 40, centro, palestrante);
        evento("Carreira em Engenharia de Software", "Conversa sobre desenvolvimento web, projetos e oportunidades profissionais.", "Auditório principal", 10, 120, centro, palestrante);
        evento("Construindo aplicações web", "Encontro sobre Spring Boot, banco de dados e interfaces para a comunidade acadêmica.", "Sala de projetos", 14, 60, centro, palestrante);
    }

    private void base(Usuario usuario, String nome, String email) {
        usuario.setNome(nome); usuario.setEmail(email); usuario.setSenha(senhas.encode("Demo1234"));
    }
    private void evento(String nome, String descricao, String local, int dias, int vagas,
                        CentroAcademico centro, Palestrante palestrante) {
        if (eventos.findAllByOrganizador_IdAndAtivoTrueOrderByDataAscHorarioAsc(centro.getId()).stream()
                .anyMatch(evento -> evento.getNome().equals(nome))) return;
        Evento evento = new Evento();
        evento.setNome(nome); evento.setDescricao(descricao); evento.setLocal(local);
        evento.setData(LocalDate.now().plusDays(dias)); evento.setHorario(LocalTime.of(19, 0));
        evento.setQuantidadeVagas(vagas); evento.setOrganizador(centro); evento.setPalestrante(palestrante);
        eventos.save(evento);
    }
}
