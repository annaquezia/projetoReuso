package br.edu.projetoreuso;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.evento.EventoService;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import br.edu.projetoreuso.usuario.UsuarioService;
import br.edu.projetoreuso.web.form.EventoForm;
import br.edu.projetoreuso.web.form.UsuarioForm;
import evento.Evento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import usuario.CentroAcademico;
import usuario.Palestrante;
import usuario.TipoUsuario;
import usuario.Universidade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.demo.seed=false")
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class RequisitosCrudTest {
    @Autowired MockMvc mvc;
    @Autowired org.springframework.context.ApplicationContext context;
    @Autowired UsuarioRepository usuarios;
    @Autowired EventoRepository eventos;
    @Autowired UsuarioService usuarioService;
    @Autowired EventoService eventoService;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactionManager;
    Universidade universidade;
    CentroAcademico centro;

    @BeforeEach
    void prepararOrganizacao() {
        String suffix = UUID.randomUUID().toString();
        universidade = new Universidade();
        universidade.setNome("Universidade de demonstração");
        universidade.setEmail("uni-" + suffix + "@exemplo.edu.br");
        universidade.setSenha(encoder.encode("SenhaTeste123"));
        universidade = usuarios.save(universidade);
        UsuarioForm form = new UsuarioForm();
        form.setNome("Centro Acadêmico"); form.setCurso("Engenharia");
        form.setEmail("ca-" + suffix + "@exemplo.edu.br"); form.setSenha("SenhaTeste123");
        form.setTipoUsuario(TipoUsuario.CENTRO_ACADEMICO);
        centro = (CentroAcademico) usuarioService.criarGerenciado(universidade.getEmail(), form);
    }

    private UsuarioForm palestranteForm(String email) {
        UsuarioForm form = new UsuarioForm();
        form.setNome("Palestrante de demonstração"); form.setEmail(email);
        form.setSenha("SenhaTeste123"); form.setTipoUsuario(TipoUsuario.PALESTRANTE);
        form.setAreaAtuacao("Tecnologia");
        return form;
    }

    private EventoForm eventoForm() {
        EventoForm form = new EventoForm();
        form.setNome("Encontro de engenharia"); form.setDescricao("Discussão sobre projetos e inovação.");
        form.setData(LocalDate.now().plusDays(7)); form.setHorario(LocalTime.of(19, 0));
        form.setLocal("Auditório"); form.setQuantidadeVagas(40);
        return form;
    }

    @Test
    void renderizaAsOitoTelasComRegistrosReais() throws Exception {
        Palestrante palestrante = (Palestrante) usuarioService.criarGerenciado(centro.getEmail(),
                palestranteForm("p-" + UUID.randomUUID() + "@exemplo.edu.br"));
        Evento evento = eventoService.criar(centro.getEmail(), eventoForm());
        String[][] telas = {
                {"/usuarios", "usuarios/lista", "Usuários da organização"},
                {"/usuarios/novo", "usuarios/novo", "Novo usuário"},
                {"/usuarios/" + palestrante.getId() + "/editar", "usuarios/editar", "Editar usuário"},
                {"/usuarios/" + palestrante.getId() + "/desativar", "usuarios/desativar", "Confirmar desativação"},
                {"/centro-academico/eventos", "eventos/gestao-lista", "Seus eventos"},
                {"/centro-academico/eventos/novo", "eventos/novo", "Criar evento"},
                {"/centro-academico/eventos/" + evento.getId() + "/editar", "eventos/editar", "Editar evento"},
                {"/centro-academico/eventos/" + evento.getId() + "/arquivar", "eventos/arquivar", "Confirmar arquivamento"}
        };
        for (String[] tela : telas) {
            var resposta = mvc.perform(get(tela[0]).with(user(centro)))
                    .andExpect(status().isOk()).andExpect(view().name(tela[1]))
                    .andExpect(content().string(containsString(tela[2])))
                    .andExpect(content().string(containsString("app-sidebar")));
            if (tela[1].endsWith("/novo") || tela[1].endsWith("/editar")) {
                String tipo = tela[1].startsWith("usuarios") ? "usuario" : "evento";
                resposta.andExpect(content().string(containsString("data-form-observer=\"" + tipo + "\"")))
                        .andExpect(content().string(containsString("data-form-preview=\"" + tipo + "\"")));
            }
        }
        mvc.perform(get("/js/previas-formularios.js"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("conectarPrevias")));
        mvc.perform(get("/eventos/" + evento.getId()))
                .andExpect(status().isOk()).andExpect(content().string(containsString(evento.getNome())));
    }

    @Test
    void executaCriacaoEdicaoEDesativacaoDeUsuarioPelasRotas() throws Exception {
        String email = "novo-" + UUID.randomUUID() + "@exemplo.edu.br";
        mvc.perform(post("/usuarios/novo").with(user(centro)).with(csrf())
                        .param("nome", "Maria Silva").param("email", email)
                        .param("senha", "SenhaTeste123").param("tipoUsuario", "PALESTRANTE")
                        .param("areaAtuacao", "Java"))
                .andExpect(redirectedUrl("/usuarios"));
        Long id = usuarios.findByEmailIgnoreCase(email).orElseThrow().getId();
        mvc.perform(post("/usuarios/" + id + "/editar").with(user(centro)).with(csrf())
                        .param("nome", "Maria Atualizada").param("email", email)
                        .param("tipoUsuario", "PALESTRANTE").param("areaAtuacao", "Web"))
                .andExpect(redirectedUrl("/usuarios"));
        assertThat(usuarios.findById(id).orElseThrow().getNome()).isEqualTo("Maria Atualizada");
        mvc.perform(post("/usuarios/" + id + "/desativar").with(user(centro)).with(csrf()))
                .andExpect(redirectedUrl("/usuarios"));
        assertThat(usuarios.findById(id).orElseThrow().isAtivo()).isFalse();
        mvc.perform(post("/login").with(csrf()).param("username", email).param("password", "SenhaTeste123"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void criaEditaEArquivaEventoSemApagarHistorico() throws Exception {

        mvc.perform(post("/centro-academico/eventos/novo").with(user(centro)).with(csrf())
                        .param("nome", "Evento pelo formulário").param("descricao", "Descrição do evento")
                        .param("data", LocalDate.now().plusDays(7).toString()).param("horario", "19:00")
                        .param("local", "Auditório").param("quantidadeVagas", "30"))
                .andExpect(status().is3xxRedirection());
        Evento evento = eventoService.listarDoCentro(centro.getEmail()).get(0);
        mvc.perform(post("/centro-academico/eventos/" + evento.getId() + "/editar").with(user(centro)).with(csrf())
                        .param("nome", "Evento atualizado").param("descricao", "Descrição atualizada")
                        .param("data", LocalDate.now().plusDays(8).toString()).param("horario", "20:00")
                        .param("local", "Sala 2").param("quantidadeVagas", "50"))
                .andExpect(redirectedUrl("/centro-academico/eventos"));
        assertThat(eventoService.buscarPublico(evento.getId()).getNome()).isEqualTo("Evento atualizado");
        mvc.perform(post("/centro-academico/eventos/" + evento.getId() + "/arquivar").with(user(centro)).with(csrf()))
                .andExpect(redirectedUrl("/centro-academico/eventos"));
        assertThat(eventos.findById(evento.getId()).orElseThrow().isAtivo()).isFalse();
        assertThat(eventoService.listarDoCentro(centro.getEmail())).isEmpty();
        assertThat(eventoService.listarPublicos(null)).noneMatch(item -> item.getId().equals(evento.getId()));
        mvc.perform(get("/eventos/" + evento.getId())).andExpect(status().isNotFound());

    }

    @Test
    void templatesRespeitamRollbackDaTransacao() {
        TransactionTemplate transacao = new TransactionTemplate(transactionManager);
        Long revertido = transacao.execute(status -> {
            Evento evento = eventoService.criar(centro.getEmail(), eventoForm());
            status.setRollbackOnly();
            return evento.getId();
        });
        assertThat(eventos.findById(revertido)).isEmpty();
        Evento confirmado = eventoService.criar(centro.getEmail(), eventoForm());
        assertThat(eventos.findById(confirmado.getId())).isPresent();
    }

    @Test
    void impedeAlterarOuArquivarEventoDeOutroCentroEExigeCsrf() throws Exception {
        Evento evento = eventoService.criar(centro.getEmail(), eventoForm());
        UsuarioForm outroForm = new UsuarioForm();
        outroForm.setNome("Outro Centro"); outroForm.setCurso("Direito");
        outroForm.setEmail("outro-" + UUID.randomUUID() + "@exemplo.edu.br");
        outroForm.setSenha("SenhaTeste123"); outroForm.setTipoUsuario(TipoUsuario.CENTRO_ACADEMICO);
        CentroAcademico outro = (CentroAcademico) usuarioService.criarGerenciado(universidade.getEmail(), outroForm);
        assertThatThrownBy(() -> eventoService.atualizar(outro.getEmail(), evento.getId(), eventoForm()))
                .isInstanceOf(AccessDeniedException.class);
        mvc.perform(get("/centro-academico/eventos/" + evento.getId() + "/arquivar").with(user(outro)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/centro-academico/eventos/" + evento.getId() + "/arquivar").with(user(outro)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/centro-academico/eventos/" + evento.getId() + "/arquivar").with(user(centro)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/erro?status=403").with(user(centro)))
                .andExpect(status().isForbidden()).andExpect(content().string(containsString("Acesso não autorizado")));
        assertThat(eventos.findById(evento.getId()).orElseThrow().isAtivo()).isTrue();
    }

    @Test
    void rejeitaDadosInvalidosSemPersistir() throws Exception {

        mvc.perform(post("/centro-academico/eventos/novo").with(user(centro)).with(csrf())
                        .param("nome", "").param("descricao", "Descrição")
                        .param("data", LocalDate.now().plusDays(1).toString()).param("horario", "19:00")
                        .param("local", "Auditório").param("quantidadeVagas", "0"))
                .andExpect(status().isOk()).andExpect(view().name("eventos/novo"))
                .andExpect(model().attributeHasFieldErrors("form", "nome", "quantidadeVagas"));
        assertThat(eventoService.listarDoCentro(centro.getEmail())).isEmpty();

        UsuarioForm duplicado = palestranteForm(centro.getEmail());
        assertThatThrownBy(() -> usuarioService.criarGerenciado(centro.getEmail(), duplicado))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("e-mail");
    }

    @Test
    void osServicosDasTelasSaoSingletonsDoContextoSpring() {
        assertThat(context.isSingleton("usuarioService")).isTrue();
        assertThat(context.isSingleton("eventoService")).isTrue();
        assertThat(context.getBean(UsuarioService.class)).isSameAs(usuarioService);
        assertThat(context.getBean(EventoService.class)).isSameAs(eventoService);
    }

    @Test
    void previasJavaSaoExibidasSemGravarERespeitamPermissoes() throws Exception {
        long usuariosAntes = usuarios.count(), eventosAntes = eventos.count();
        mvc.perform(post("/usuarios/previa").with(user(centro)).with(csrf())
                        .contentType("application/json")
                        .content("{\"nome\":\"Ana\",\"tipoUsuario\":\"PALESTRANTE\",\"areaAtuacao\":\"Java\",\"senha\":\"nao-exibir\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.perfil").value("Palestrante")).andExpect(jsonPath("$.detalhe").value("Java"))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(post("/centro-academico/eventos/previa").with(user(centro)).with(csrf())
                        .contentType("application/json")
                        .content("{\"nome\":\"Evento\",\"data\":\"2026-10-20\",\"horario\":\"19:00\",\"quantidadeVagas\":\"40\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quando").value("20/10/2026 · 19:00"))
                .andExpect(jsonPath("$.vagas").value("40 vagas"));
        mvc.perform(post("/minha-conta/previa").with(user("aluno").roles("ALUNO")).with(csrf())
                        .contentType("application/json").content("{\"nome\":\"Estudante\",\"tipoUsuario\":\"ALUNO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("Estudante"));
        mvc.perform(post("/usuarios/previa").with(user("aluno").roles("ALUNO")).with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/centro-academico/eventos/previa").with(user(universidade)).with(csrf())
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/usuarios/previa").with(user(centro)).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        assertThat(usuarios.count()).isEqualTo(usuariosAntes);
        assertThat(eventos.count()).isEqualTo(eventosAntes);
    }
}
