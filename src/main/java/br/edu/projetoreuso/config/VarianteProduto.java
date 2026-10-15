package br.edu.projetoreuso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Ponto de variação da LPS: divulgação de eventos com ou sem inscrições. */
@Component
public class VarianteProduto {
    private final boolean inscricoesHabilitadas;

    public VarianteProduto(@Value("${app.inscricoes.habilitadas:true}") boolean inscricoesHabilitadas) {
        this.inscricoesHabilitadas = inscricoesHabilitadas;
    }

    public boolean isInscricoesHabilitadas() { return inscricoesHabilitadas; }

    public void exigirInscricoes() {
        if (!inscricoesHabilitadas) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Este catálogo não oferece inscrições pela plataforma.");
        }
    }
}
