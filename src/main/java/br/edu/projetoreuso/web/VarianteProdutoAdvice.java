package br.edu.projetoreuso.web;

import br.edu.projetoreuso.config.VarianteProduto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class VarianteProdutoAdvice {
    private final VarianteProduto variante;

    public VarianteProdutoAdvice(VarianteProduto variante) { this.variante = variante; }

    @ModelAttribute("inscricoesHabilitadas")
    public boolean inscricoesHabilitadas() { return variante.isInscricoesHabilitadas(); }
}
