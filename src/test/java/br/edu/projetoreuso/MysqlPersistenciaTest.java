package br.edu.projetoreuso;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.inscricao.InscricaoRepository;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import br.edu.projetoreuso.usuario.UsuarioService;
import br.edu.projetoreuso.web.form.UsuarioForm;
import evento.EventoStatus;
import evento.state.DisponivelState;
import inscricao.state.InscricaoAtivaState;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import usuario.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Opt-in: usa o esquema MySQL existente e remove somente os registros que o teste criou. */
@EnabledIfEnvironmentVariable(named = "VALIDAR_MYSQL", matches = "true")
@SpringBootTest(properties = {"app.demo.seed=false", "APP_BOOTSTRAP_EMAIL=", "APP_BOOTSTRAP_PASSWORD="})
@AutoConfigureMockMvc
class MysqlPersistenciaTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate sql;
    @Autowired UsuarioRepository usuarios;
    @Autowired EventoRepository eventos;
    @Autowired InscricaoRepository inscricoes;
    @Autowired UsuarioService usuarioService;
    @Autowired PasswordEncoder senhas;
    private final List<Long> usuariosCriados = new ArrayList<>();
    private final List<Long> eventosCriados = new ArrayList<>();
    private Map<String, Long> antes;
    private Universidade universidade;
    private CentroAcademico centro;
    private final String marca = "auditoria-" + UUID.randomUUID();

    @BeforeEach
    void preparar() {
        antes = contagens();
        universidade = new Universidade();
        universidade.setNome("Universidade " + marca);
        universidade.setEmail("uni-" + marca + "@teste.invalid");
        universidade.setSenha(senhas.encode("Teste1234"));
        universidade = usuarios.save(universidade);
        usuariosCriados.add(universidade.getId());
        UsuarioForm form = new UsuarioForm();
        form.setNome("Centro " + marca);
        form.setEmail("centro-" + marca + "@teste.invalid");
        form.setSenha("Teste1234");
        form.setCurso("Engenharia");
        form.setTipoUsuario(TipoUsuario.CENTRO_ACADEMICO);
        centro = (CentroAcademico) usuarioService.criarGerenciado(universidade.getEmail(), form);
        usuariosCriados.add(centro.getId());
    }

    @AfterEach
    void removerSomenteDadosDesteTeste() {
        for (Long id : eventosCriados) {
            sql.update("DELETE FROM inscricao_evento WHERE evento_id = ?", id);
            sql.update("DELETE FROM evento WHERE id = ?", id);
        }
        Collections.reverse(usuariosCriados);
        for (Long id : usuariosCriados) {
            for (String tabela : List.of("palestrante", "estudante", "centro_academico", "universidade", "usuario"))
                sql.update("DELETE FROM " + tabela + " WHERE id = ?", id);
        }
        assertThat(contagens()).isEqualTo(antes);
    }

    private Map<String, Long> contagens() {
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (String tabela : List.of("usuario", "universidade", "centro_academico", "palestrante", "estudante", "evento", "inscricao_evento"))
            resultado.put(tabela, sql.queryForObject("SELECT COUNT(*) FROM " + tabela, Long.class));
        return resultado;
    }

    private long criarEvento(String nome) throws Exception {
        mvc.perform(post("/centro-academico/eventos/novo").with(user(centro)).with(csrf())
                        .param("nome", nome).param("descricao", "Verificação MySQL")
                        .param("data", LocalDate.now().plusDays(7).toString()).param("horario", "19:00")
                        .param("local", "Sala de teste").param("quantidadeVagas", "1"))
                .andExpect(status().is3xxRedirection());
        long id = sql.queryForObject("SELECT id FROM evento WHERE nome = ? AND centro_academico_id = ?", Long.class, nome, centro.getId());
        eventosCriados.add(id);
        return id;
    }

    @Test
    void crudPelasRotasConfereCadaOperacaoEmSql() throws Exception {
        mvc.perform(get("/usuarios").with(user(centro))).andExpect(status().isOk());
        mvc.perform(get("/centro-academico/eventos").with(user(centro))).andExpect(status().isOk());
        String email = marca + "@teste.invalid";
        mvc.perform(post("/usuarios/novo").with(user(centro)).with(csrf())
                        .param("nome", "Pessoa " + marca).param("email", email).param("senha", "Teste1234")
                        .param("tipoUsuario", "PALESTRANTE").param("areaAtuacao", "Java"))
                .andExpect(redirectedUrl("/usuarios"));
        var pessoa = usuarios.findByEmailIgnoreCase(email).orElseThrow();
        usuariosCriados.add(pessoa.getId());
        assertThat(sql.queryForObject("SELECT nome FROM usuario WHERE id = ?", String.class, pessoa.getId())).isEqualTo("Pessoa " + marca);
        mvc.perform(post("/usuarios/" + pessoa.getId() + "/editar").with(user(centro)).with(csrf())
                        .param("nome", "Pessoa atualizada").param("email", email).param("tipoUsuario", "PALESTRANTE")
                        .param("areaAtuacao", "Spring"))
                .andExpect(redirectedUrl("/usuarios"));
        assertThat(sql.queryForObject("SELECT nome FROM usuario WHERE id = ?", String.class, pessoa.getId())).isEqualTo("Pessoa atualizada");
        assertThat(sql.queryForObject("SELECT area_atuacao FROM palestrante WHERE id = ?", String.class, pessoa.getId())).isEqualTo("Spring");
        mvc.perform(post("/usuarios/" + pessoa.getId() + "/excluir").with(user(centro)).with(csrf()))
                .andExpect(redirectedUrl("/usuarios"));
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM usuario WHERE id = ?", Long.class, pessoa.getId())).isZero();
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM palestrante WHERE id = ?", Long.class, pessoa.getId())).isZero();

        long evento = criarEvento(marca);
        mvc.perform(get("/eventos/" + evento)).andExpect(status().isOk());
        mvc.perform(post("/centro-academico/eventos/" + evento + "/editar").with(user(centro)).with(csrf())
                        .param("nome", marca + " atualizado").param("descricao", "Persistência comprovada")
                        .param("data", LocalDate.now().plusDays(8).toString()).param("horario", "20:00")
                        .param("local", "Sala atualizada").param("quantidadeVagas", "2"))
                .andExpect(redirectedUrl("/centro-academico/eventos"));
        assertThat(sql.queryForObject("SELECT local FROM evento WHERE id = ?", String.class, evento)).isEqualTo("Sala atualizada");
        mvc.perform(post("/centro-academico/eventos/" + evento + "/excluir").with(user(centro)).with(csrf()))
                .andExpect(redirectedUrl("/centro-academico/eventos"));
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM evento WHERE id = ?", Long.class, evento)).isZero();
    }

    @Test
    void stateRestauraDoMysqlEHistoricoImpedeExclusao() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setNome("Estudante " + marca);
        aluno.setEmail("aluno-" + marca + "@teste.invalid");
        aluno.setSenha(senhas.encode("Teste1234"));
        aluno.setMatricula(UUID.randomUUID().toString());
        aluno.setCurso("Engenharia");
        aluno = usuarios.save(aluno);
        usuariosCriados.add(aluno.getId());
        long evento = criarEvento(marca);
        mvc.perform(post("/eventos/" + evento + "/inscrever").with(user(aluno)).with(csrf()))
                .andExpect(flash().attributeExists("sucesso"));
        long id = sql.queryForObject("SELECT id FROM inscricao_evento WHERE aluno_id = ? AND evento_id = ?", Long.class, aluno.getId(), evento);
        assertThat(sql.queryForObject("SELECT status FROM evento WHERE id = ?", String.class, evento)).isEqualTo("LOTADO");
        mvc.perform(post("/minhas-inscricoes/" + id + "/cancelar").with(user(aluno)).with(csrf()))
                .andExpect(flash().attributeExists("sucesso"));
        assertThat(sql.queryForObject("SELECT ativa FROM inscricao_evento WHERE id = ?", Boolean.class, id)).isFalse();
        assertThat(inscricoes.findById(id).orElseThrow().getEstado()).isInstanceOf(inscricao.state.InscricaoCanceladaState.class);
        assertThat(eventos.findById(evento).orElseThrow().getStatus()).isEqualTo(EventoStatus.DISPONIVEL);
        assertThat(eventos.findById(evento).orElseThrow().getEventoState()).isSameAs(DisponivelState.getInstance());
        mvc.perform(post("/eventos/" + evento + "/inscrever").with(user(aluno)).with(csrf()))
                .andExpect(flash().attributeExists("sucesso"));
        assertThat(sql.queryForObject("SELECT ativa FROM inscricao_evento WHERE id = ?", Boolean.class, id)).isTrue();
        assertThat(inscricoes.findById(id).orElseThrow().getEstado()).isSameAs(InscricaoAtivaState.getInstance());
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM inscricao_evento WHERE evento_id = ?", Long.class, evento)).isEqualTo(1L);
        mvc.perform(post("/centro-academico/eventos/" + evento + "/excluir").with(user(centro)).with(csrf()))
                .andExpect(flash().attributeExists("erro"));
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM evento WHERE id = ?", Long.class, evento)).isEqualTo(1L);
    }
}
