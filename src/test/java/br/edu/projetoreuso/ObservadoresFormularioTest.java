package br.edu.projetoreuso;

import br.edu.projetoreuso.patterns.observer.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ObservadoresFormularioTest {
    @Test
    void subjectNotificaInscritosEPermiteCancelarSemDuplicarInscricao() {
        FormularioSubject<String> subject = new FormularioSubject<>();
        List<String> primeiro = new ArrayList<>();
        List<String> segundo = new ArrayList<>();
        FormularioObserver<String> a = primeiro::add;
        FormularioObserver<String> b = segundo::add;
        subject.registrarObserver(a); subject.registrarObserver(a); subject.registrarObserver(b);
        subject.alterar("rascunho 1");
        subject.removerObserver(a);
        subject.alterar("rascunho 2");
        assertThat(primeiro).containsExactly("rascunho 1");
        assertThat(segundo).containsExactly("rascunho 1", "rascunho 2");
    }

    @Test
    void observerUsuarioAtualizaPreviaSemCompartilharDadosEntreFormularios() {
        FormularioSubject<DadosUsuarioPrevia> subject = new FormularioSubject<>();
        PreviaUsuarioObserver observer = new PreviaUsuarioObserver();
        PreviaUsuarioObserver outraTela = new PreviaUsuarioObserver();
        subject.registrarObserver(observer);
        subject.alterar(new DadosUsuarioPrevia(" Ana ", "ana@exemplo.edu.br", "PALESTRANTE", null, "Java"));
        var antiga = observer.getPrevia();
        assertThat(antiga).containsEntry("nome", "Ana").containsEntry("perfil", "Palestrante").containsEntry("detalhe", "Java");
        subject.alterar(new DadosUsuarioPrevia("<script>texto</script>", "", "CENTRO_ACADEMICO", "Engenharia", null));
        assertThat(observer.getPrevia()).containsEntry("nome", "<script>texto</script>")
                .containsEntry("email", "E-mail ainda não informado").containsEntry("detalhe", "Engenharia");
        assertThat(antiga).containsEntry("nome", "Ana");
        assertThat(outraTela.getPrevia()).isEmpty();
        assertThatThrownBy(() -> observer.getPrevia().put("senha", "segredo")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void observerEventoFormataAgendaEVagasEAceitaRascunhosIncompletos() {
        FormularioSubject<DadosEventoPrevia> subject = new FormularioSubject<>();
        PreviaEventoObserver observer = new PreviaEventoObserver();
        subject.registrarObserver(observer);
        subject.alterar(new DadosEventoPrevia("Encontro", "Sobre Java", "2026-10-20", "19:00", "Sala 2", "40"));
        assertThat(observer.getPrevia()).containsEntry("quando", "20/10/2026 · 19:00").containsEntry("vagas", "40 vagas");
        subject.alterar(new DadosEventoPrevia(null, null, "2026-02-30", "25:00", "", "1"));
        assertThat(observer.getPrevia()).containsEntry("quando", "Data a definir · Horário a definir")
                .containsEntry("vagas", "1 vaga").containsEntry("nome", "Nome do evento");
        subject.alterar(new DadosEventoPrevia("Novo", "", "", "", "", "-2"));
        assertThat(observer.getPrevia()).containsEntry("vagas", "Informe as vagas").containsEntry("nome", "Novo");
    }
}
