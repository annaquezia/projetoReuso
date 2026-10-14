package br.edu.projetoreuso.web;

import br.edu.projetoreuso.inscricao.InscricaoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InscricoesController {
    private final InscricaoService inscricoes;
    public InscricoesController(InscricaoService inscricoes) { this.inscricoes = inscricoes; }
    @PostMapping("/eventos/{id}/inscrever")
    public String inscrever(Authentication auth, @PathVariable Long id, RedirectAttributes flash) {
        try {
            inscricoes.inscrever(auth.getName(), id);
            flash.addFlashAttribute("sucesso", "Inscrição confirmada! Consulte Minhas inscrições para acompanhar seu evento.");
        } catch (IllegalStateException ex) { flash.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/eventos/" + id;
    }
    @GetMapping("/minhas-inscricoes")
    public String minhas(Authentication auth, Model model) {
        model.addAttribute("inscricoes", inscricoes.minhas(auth.getName()));
        return "inscricoes/lista";
    }
    @PostMapping("/minhas-inscricoes/{id}/cancelar")
    public String cancelar(Authentication auth, @PathVariable Long id, RedirectAttributes flash) {
        try {
            inscricoes.cancelar(auth.getName(), id);
            flash.addFlashAttribute("sucesso", "Inscrição cancelada.");
        } catch (IllegalStateException ex) { flash.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/minhas-inscricoes";
    }
}
