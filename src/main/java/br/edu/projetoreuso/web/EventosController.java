package br.edu.projetoreuso.web;

import br.edu.projetoreuso.evento.EventoService;
import evento.EventoStatus;
import br.edu.projetoreuso.inscricao.InscricaoService;
import br.edu.projetoreuso.organizacao.ComunicadoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventosController {
    private final EventoService eventos;
    private final InscricaoService inscricoes;
    private final ComunicadoService conteudo;

    public EventosController(EventoService eventos, InscricaoService inscricoes, ComunicadoService conteudo) {
        this.eventos = eventos;
        this.inscricoes = inscricoes;
        this.conteudo = conteudo;
    }

    @GetMapping("/eventos")
    public String listar(@RequestParam(required = false) EventoStatus status, Model model) {
        model.addAttribute("eventos", eventos.listarPublicos(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusOptions", EventoStatus.values());
        return "eventos/lista-publica";
    }

    @GetMapping("/eventos/{id}")
    public String detalhe(@PathVariable Long id, Model model, Authentication authentication) {
        var evento = eventos.buscarPublico(id);
        model.addAttribute("evento", evento);
        model.addAttribute("comunicados", conteudo.publicados(id));
        model.addAttribute("situacao", inscricoes.situacao(evento, authentication == null ? null : authentication.getName()));
        return "eventos/detalhe";
    }
}
