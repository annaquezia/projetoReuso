package br.edu.projetoreuso;

import br.edu.projetoreuso.config.DadosDemonstracao;
import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import usuario.*;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"app.demo.seed=true", "spring.datasource.url=jdbc:h2:mem:demo_seed;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DadosDemonstracaoTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired MockMvc mvc;
    @Autowired EventoRepository eventos;
    @Autowired PasswordEncoder senhas;
    @Autowired DadosDemonstracao dados;

    @Test
    void preparaContasEventosEVinculosSemDuplicarDados() {
        assertThat(usuarios.findByEmailIgnoreCase("universidade@demo.com").orElseThrow()).isInstanceOf(Universidade.class);
        assertThat(usuarios.findByEmailIgnoreCase("centro@demo.com").orElseThrow()).isInstanceOf(CentroAcademico.class);
        assertThat(usuarios.findByEmailIgnoreCase("usuario@demo.com").orElseThrow()).isInstanceOf(Aluno.class);
        for (String email : new String[]{"universidade@demo.com", "centro@demo.com", "usuario@demo.com", "palestrante@demo.com"}) {
            var usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow();
            assertThat(usuario.isAtivo()).isTrue();
            assertThat(senhas.matches("Demo1234", usuario.getSenha())).isTrue();
        }
        var agenda = eventos.findAllByAtivoTrueOrderByDataAscHorarioAsc();
        assertThat(agenda).hasSize(3);
        assertThat(agenda).allSatisfy(evento -> {
            assertThat(evento.getData()).isAfter(LocalDate.now());
            assertThat(evento.getOrganizador().getEmail()).isEqualTo("centro@demo.com");
            assertThat(evento.getPalestrante().getEmail()).isEqualTo("palestrante@demo.com");
        });
        dados.run(new DefaultApplicationArguments(new String[0]));
        assertThat(usuarios.count()).isEqualTo(4);
        assertThat(eventos.count()).isEqualTo(3);
    }

    @Test
    void osTresPerfisEntramNoSiteComAgendaPreenchida() throws Exception {
        Long eventoId = eventos.findAllByAtivoTrueOrderByDataAscHorarioAsc().get(0).getId();
        String[][] perfis = {
            {"universidade@demo.com", "/usuarios", "Centro Acadêmico de Engenharia"},
            {"centro@demo.com", "/centro-academico/eventos", "Design patterns na prática com Java"},
            {"usuario@demo.com", "/eventos/" + eventoId, "Inscrever-me"}
        };
        for (String[] perfil : perfis) {
            var resultado = mvc.perform(post("/login").with(csrf()).param("username", perfil[0]).param("password", "Demo1234"))
                    .andExpect(redirectedUrl("/painel")).andReturn();
            mvc.perform(get(perfil[1]).session((MockHttpSession) resultado.getRequest().getSession(false)))
                    .andExpect(status().isOk()).andExpect(content().string(containsString(perfil[2])));
        }
    }
}
