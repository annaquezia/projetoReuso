package br.edu.projetoreuso;

import br.edu.projetoreuso.evento.EventoService;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import br.edu.projetoreuso.web.form.EventoForm;
import evento.Evento;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import usuario.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.demo.seed=false",
        "spring.datasource.url=jdbc:h2:mem:comunicados_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class ComunicadosTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate sql;
    @Autowired UsuarioRepository usuarios;
    @Autowired EventoService eventos;
    @Autowired PasswordEncoder encoder;
    private final List<Long> usuariosCriados = new ArrayList<>();
    private Map<String, Long> antes;
    private CentroAcademico centro, outroCentro;
    private Aluno aluno;
    private Evento evento;
    private final String marca = "comunicado-" + UUID.randomUUID();

    @BeforeEach
    void preparar() {
        antes = contagens();
        Universidade uni = new Universidade();
        base(uni, "Universidade");
        uni = salvar(uni);
        centro = centro(uni);
        outroCentro = centro(uni);
        aluno = new Aluno();
        base(aluno, "Estudante");
        aluno.setMatricula(UUID.randomUUID().toString());
        aluno.setCurso("Engenharia");
        aluno = salvar(aluno);
        EventoForm form = new EventoForm();
        form.setNome("Evento " + marca);
        form.setDescricao("Evento de verificação");
        form.setData(LocalDate.now().plusDays(7));
        form.setHorario(LocalTime.of(10, 0));
        form.setLocal("Sala de projetos");
        form.setQuantidadeVagas(5);
        evento = eventos.criar(centro.getEmail(), form);
    }

    private void base(Usuario usuario, String nome) {
        usuario.setNome(nome + " " + marca);
        usuario.setEmail(UUID.randomUUID() + "@teste.invalid");
        usuario.setSenha(encoder.encode("Teste1234"));
    }

    private <T extends Usuario> T salvar(T usuario) {
        T salvo = usuarios.save(usuario);
        usuariosCriados.add(salvo.getId());
        return salvo;
    }

    private CentroAcademico centro(Universidade uni) {
        CentroAcademico item = new CentroAcademico();
        base(item, "Centro");
        item.setCurso("Engenharia");
        item.setUniversidade(uni);
        return salvar(item);
    }

    private Map<String, Long> contagens() {
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (String tabela : List.of("usuario", "universidade", "centro_academico", "estudante",
                "palestrante", "evento", "inscricao_evento", "comunicado"))
            resultado.put(tabela, sql.queryForObject("SELECT COUNT(*) FROM " + tabela, Long.class));
        return resultado;
    }

    @AfterEach
    void limparSomenteRegistrosDoTeste() {
        if (evento != null) {
            sql.update("DELETE FROM comunicado WHERE evento_id=?", evento.getId());
            sql.update("DELETE FROM evento WHERE id=?", evento.getId());
        }
        Collections.reverse(usuariosCriados);
        for (Long id : usuariosCriados)
            for (String tabela : List.of("estudante", "centro_academico", "universidade", "usuario"))
                sql.update("DELETE FROM " + tabela + " WHERE id=?", id);
        assertThat(contagens()).isEqualTo(antes);
    }

    private MockHttpServletRequestBuilder publicar(String rota, String mensagem) {
        return post(rota).with(user(centro)).with(csrf()).param("titulo", marca)
                .param("mensagem", mensagem).param("eventoId", evento.getId().toString());
    }

    private Long idCriado() {
        return sql.queryForObject("SELECT id FROM comunicado WHERE titulo=?", Long.class, marca);
    }

    @Test
    void cicloCompletoConfereSqlPermissoesEConfirmacao() throws Exception {
        String rota = "/centro-academico/comunicados";
        mvc.perform(get(rota).with(user(centro))).andExpect(status().isOk());
        mvc.perform(get(rota + "/novo").with(user(centro))).andExpect(status().isOk());
        mvc.perform(publicar(rota + "/novo", "Orientação inicial")).andExpect(redirectedUrl(rota));
        Long id = idCriado();
        assertThat(sql.queryForObject("SELECT mensagem FROM comunicado WHERE id=?", String.class, id)).isEqualTo("Orientação inicial");
        mvc.perform(get(rota + "/" + id + "/editar").with(user(centro))).andExpect(status().isOk());
        mvc.perform(get(rota + "/" + id + "/editar").with(user(outroCentro))).andExpect(status().isForbidden());
        mvc.perform(get(rota).with(user(aluno))).andExpect(status().isForbidden());
        mvc.perform(publicar(rota + "/" + id + "/editar", "Orientação atualizada")).andExpect(redirectedUrl(rota));
        assertThat(sql.queryForObject("SELECT mensagem FROM comunicado WHERE id=?", String.class, id)).isEqualTo("Orientação atualizada");
        mvc.perform(get(rota + "/" + id + "/excluir").with(user(centro))).andExpect(status().isOk());
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM comunicado WHERE id=?", Long.class, id)).isEqualTo(1L);
        mvc.perform(post(rota + "/" + id + "/excluir").with(user(centro))).andExpect(status().isForbidden());
        mvc.perform(post(rota + "/" + id + "/excluir").with(user(outroCentro)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(rota + "/" + id + "/excluir").with(user(centro)).with(csrf())).andExpect(redirectedUrl(rota));
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM comunicado WHERE id=?", Long.class, id)).isZero();
    }

    @Test
    void comunicadoApareceNoEventoEImpedePerdaDoVinculo() throws Exception {
        String rota = "/centro-academico/comunicados/novo";
        mvc.perform(publicar(rota, "Orientação para participantes")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/eventos/" + evento.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Orientação para participantes")));
        mvc.perform(post(rota).with(user(outroCentro)).with(csrf()).param("titulo", marca)
                .param("mensagem", "Outra mensagem").param("eventoId", evento.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/centro-academico/eventos/" + evento.getId() + "/excluir").with(user(centro)).with(csrf()))
                .andExpect(flash().attributeExists("erro"));
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM evento WHERE id=?", Long.class, evento.getId())).isEqualTo(1L);
        assertThat(sql.queryForObject("SELECT COUNT(*) FROM comunicado WHERE id=?", Long.class, idCriado())).isEqualTo(1L);
    }
}
