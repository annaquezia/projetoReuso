package br.edu.projetoreuso;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.inscricao.InscricaoService;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.inscricoes.habilitadas=false",
        "spring.datasource.url=jdbc:h2:mem:catalogo_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class VarianteCatalogoTest {
    @Autowired MockMvc mvc;
    @Autowired EventoRepository eventos;
    @Autowired InscricaoService inscricoes;
    @Autowired UsuarioRepository usuarios;

    @Test
    void catalogoCompartilhaEventosEMantemInscricoesIndisponiveis() throws Exception {
        Long id = eventos.findAllByAtivoTrueOrderByDataAscHorarioAsc().get(0).getId();
        var aluno = usuarios.findByEmailIgnoreCase("usuario@demo.com").orElseThrow();
        mvc.perform(get("/eventos")).andExpect(status().isOk());
        mvc.perform(get("/eventos/" + id).with(user(aluno)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este catálogo divulga a programação")))
                .andExpect(content().string(not(containsString("Inscrever-me"))))
                .andExpect(content().string(not(containsString("Minhas inscrições"))));
        mvc.perform(post("/eventos/" + id + "/inscrever").with(user(aluno)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/minhas-inscricoes").with(user(aluno)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/minhas-inscricoes/1/cancelar").with(user(aluno)).with(csrf()))
                .andExpect(status().isForbidden());
        assertThatThrownBy(() -> inscricoes.inscrever("usuario@demo.com", id))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> inscricoes.cancelar("usuario@demo.com", 1L))
                .isInstanceOf(AccessDeniedException.class);
    }
}
