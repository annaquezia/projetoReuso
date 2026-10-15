package br.edu.projetoreuso;

import br.edu.projetoreuso.evento.*;
import br.edu.projetoreuso.inscricao.*;
import br.edu.projetoreuso.usuario.*;
import br.edu.projetoreuso.web.form.*;
import evento.Evento;
import evento.EventoStatus;
import inscricao.state.InscricaoAtivaState;
import inscricao.state.InscricaoCanceladaState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import usuario.*;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.demo.seed=false", "spring.datasource.url=jdbc:h2:mem:inscricoes_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class InscricoesTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired UsuarioService usuarioService;
    @Autowired EventoRepository eventos;
    @Autowired EventoService eventoService;
    @Autowired InscricaoService inscricoes;
    @Autowired InscricaoRepository registros;
    CentroAcademico centro;
    Aluno aluno;
    Evento evento;
    @BeforeEach void preparar() {
        Universidade uni = new Universidade();
        uni.setNome("Universidade"); uni.setEmail("uni-" + UUID.randomUUID() + "@demo.com"); uni.setSenha("hash-ficticio");
        uni = usuarios.save(uni);
        UsuarioForm form = new UsuarioForm(); form.setNome("Centro"); form.setCurso("Engenharia");
        form.setEmail("ca-" + UUID.randomUUID() + "@demo.com"); form.setSenha("Senha123"); form.setTipoUsuario(TipoUsuario.CENTRO_ACADEMICO);
        centro = (CentroAcademico) usuarioService.criarGerenciado(uni.getEmail(), form);
        aluno = novoAluno();
        evento = eventoService.criar(centro.getEmail(), formularioEvento(1));
    }
    private Aluno novoAluno() {
        CadastroAlunoForm form = new CadastroAlunoForm();
        form.setNome("Estudante"); form.setEmail("aluno-" + UUID.randomUUID() + "@demo.com"); form.setSenha("Senha123");
        form.setMatricula(UUID.randomUUID().toString()); form.setCurso("Engenharia"); form.setDataNascimento(LocalDate.of(2004, 1, 1));
        return usuarioService.cadastrarAluno(form);
    }
    private EventoForm formularioEvento(int vagas) {
        EventoForm form = new EventoForm(); form.setNome("Oficina Java"); form.setDescricao("Prática de Java");
        form.setData(LocalDate.now().plusDays(7)); form.setHorario(LocalTime.of(19, 0)); form.setLocal("Laboratório"); form.setQuantidadeVagas(vagas);
        return form;
    }
    @Test void estudanteSeInscreveConsultaECancelaPelasTelas() throws Exception {
        mvc.perform(get("/eventos/" + evento.getId()).with(user(aluno))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Inscrever-me")));
        mvc.perform(post("/eventos/" + evento.getId() + "/inscrever").with(user(aluno)).with(csrf()))
                .andExpect(redirectedUrl("/eventos/" + evento.getId())).andExpect(flash().attributeExists("sucesso"));
        var inscricao = registros.findByAluno_IdAndEvento_Id(aluno.getId(), evento.getId()).orElseThrow();
        assertThat(inscricao.isAtiva()).isTrue();
        assertThat(eventos.findById(evento.getId()).orElseThrow().getStatus()).isEqualTo(EventoStatus.LOTADO);
        mvc.perform(get("/minhas-inscricoes").with(user(aluno))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Confirmada"))).andExpect(content().string(containsString("Cancelar inscrição")));
        mvc.perform(get("/eventos/" + evento.getId()).with(user(aluno))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Sua inscrição neste evento está confirmada")));
        mvc.perform(post("/eventos/" + evento.getId() + "/inscrever").with(user(aluno)).with(csrf()))
                .andExpect(flash().attributeExists("erro"));
        assertThat(registros.countByEvento_IdAndAtivaTrue(evento.getId())).isEqualTo(1);
        mvc.perform(post("/minhas-inscricoes/" + inscricao.getId() + "/cancelar").with(user(aluno)).with(csrf()))
                .andExpect(redirectedUrl("/minhas-inscricoes"));
        assertThat(registros.findById(inscricao.getId()).orElseThrow().isAtiva()).isFalse();
        assertThat(registros.findById(inscricao.getId()).orElseThrow().getEstado())
                .isInstanceOf(InscricaoCanceladaState.class);
        assertThat(eventos.findById(evento.getId()).orElseThrow().getStatus()).isEqualTo(EventoStatus.DISPONIVEL);
        assertThat(inscricoes.inscrever(aluno.getEmail(), evento.getId()).getId()).isEqualTo(inscricao.getId());
        assertThat(registros.findById(inscricao.getId()).orElseThrow().getEstado())
                .isInstanceOf(InscricaoAtivaState.class);
    }
    @Test void protegeInscricoesPorPerfilProprietarioECsrf() throws Exception {
        var inscricao = inscricoes.inscrever(aluno.getEmail(), evento.getId());
        Aluno outro = novoAluno();
        mvc.perform(post("/minhas-inscricoes/" + inscricao.getId() + "/cancelar").with(user(outro)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/eventos/" + evento.getId() + "/inscrever").with(user(centro)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/minhas-inscricoes/" + inscricao.getId() + "/cancelar").with(user(aluno))).andExpect(status().isForbidden());
        assertThat(registros.findById(inscricao.getId()).orElseThrow().isAtiva()).isTrue();
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void inscricoesConcorrentesNaoUltrapassamCapacidade(int capacidade) throws Exception {
        eventoService.atualizar(centro.getEmail(), evento.getId(), formularioEvento(capacidade));
        java.util.List<String> emails = new java.util.ArrayList<>();
        emails.add(aluno.getEmail());
        for (int i = 0; i < capacidade; i++) emails.add(novoAluno().getEmail());
        ExecutorService executor = Executors.newFixedThreadPool(emails.size());
        CountDownLatch prontos = new CountDownLatch(emails.size()), inicio = new CountDownLatch(1);
        try {
            java.util.List<Future<Boolean>> tentativas = new java.util.ArrayList<>();
            for (String email : emails) tentativas.add(executor.submit(tentativa(email, prontos, inicio)));
            assertThat(prontos.await(5, TimeUnit.SECONDS)).isTrue(); inicio.countDown();
            int confirmadas = 0;
            for (Future<Boolean> tentativa : tentativas) if (tentativa.get(10, TimeUnit.SECONDS)) confirmadas++;
            assertThat(confirmadas).isEqualTo(capacidade);
            assertThat(registros.countByEvento_IdAndAtivaTrue(evento.getId())).isEqualTo(capacidade);
        } finally { inicio.countDown(); executor.shutdownNow(); }
    }
    private Callable<Boolean> tentativa(String email, CountDownLatch prontos, CountDownLatch inicio) {
        return () -> { prontos.countDown(); inicio.await();
            try { inscricoes.inscrever(email, evento.getId()); return true; }
            catch (IllegalStateException ex) { return false; }
        };
    }
    @Test void impedeDiminuirCapacidadeAbaixoDasInscricoesEReabreQuandoAumenta() {
        inscricoes.inscrever(aluno.getEmail(), evento.getId());
        eventoService.atualizar(centro.getEmail(), evento.getId(), formularioEvento(2));
        assertThat(eventos.findById(evento.getId()).orElseThrow().getStatus()).isEqualTo(EventoStatus.DISPONIVEL);
        inscricoes.inscrever(novoAluno().getEmail(), evento.getId());
        assertThatThrownBy(() -> eventoService.atualizar(centro.getEmail(), evento.getId(), formularioEvento(1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("inscrições confirmadas");
        assertThat(eventos.findById(evento.getId()).orElseThrow().getQuantidadeVagas()).isEqualTo(2);
    }
    @Test void eventosCanceladosArquivadosOuPassadosNaoAceitamInscricao() {
        eventoService.cancelar(centro.getEmail(), evento.getId());
        assertThatThrownBy(() -> inscricoes.inscrever(aluno.getEmail(), evento.getId())).isInstanceOf(IllegalStateException.class);
        Evento arquivado = eventoService.criar(centro.getEmail(), formularioEvento(3));
        eventoService.arquivar(centro.getEmail(), arquivado.getId());
        assertThatThrownBy(() -> inscricoes.inscrever(aluno.getEmail(), arquivado.getId())).isInstanceOf(IllegalArgumentException.class);
        Evento passado = eventoService.criar(centro.getEmail(), formularioEvento(3));
        passado.setData(LocalDate.now().minusDays(1)); eventos.save(passado);
        assertThatThrownBy(() -> inscricoes.inscrever(aluno.getEmail(), passado.getId())).isInstanceOf(IllegalStateException.class);
        assertThat(registros.countByEvento_IdAndAtivaTrue(evento.getId())).isZero();
    }

    @Test void minhaContaRecusaMatriculaDuplicadaOuVaziaSemAlterarORegistro() {
        Aluno outro = novoAluno();
        UsuarioForm form = UsuarioForm.from(aluno);
        form.setMatricula(" " + outro.getMatricula() + " ");
        assertThatThrownBy(() -> usuarioService.atualizarMinhaConta(aluno.getEmail(), form))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("matrícula");
        form.setMatricula("   ");
        assertThatThrownBy(() -> usuarioService.atualizarMinhaConta(aluno.getEmail(), form))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("matrícula");
        assertThat(usuarios.findById(aluno.getId()).map(Aluno.class::cast).orElseThrow().getMatricula())
                .isEqualTo(aluno.getMatricula());
    }
}
