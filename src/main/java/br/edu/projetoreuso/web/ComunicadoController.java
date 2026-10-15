package br.edu.projetoreuso.web;

import br.edu.projetoreuso.organizacao.ComunicadoService;
import br.edu.projetoreuso.evento.EventoService;
import br.edu.projetoreuso.web.form.ComunicadoForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/centro-academico/comunicados")
public class ComunicadoController {
    private static final String BASE = "/centro-academico/comunicados";
    private final ComunicadoService registros;
    private final EventoService eventos;

    public ComunicadoController(ComunicadoService registros, EventoService eventos) {
        this.registros = registros;
        this.eventos = eventos;
    }

    @GetMapping
    public String listar(Authentication ator, Model model) {
        model.addAttribute("comunicados", registros.listar(ator.getName()));
        return "comunicados/lista";
    }

    @GetMapping("/novo")
    public String novo(Authentication ator, Model model) {
        return preparar(ator.getName(), model, new ComunicadoForm(), BASE + "/novo", "Novo comunicado");
    }

    @PostMapping("/novo")
    public String criar(Authentication ator, @Valid @ModelAttribute("form") ComunicadoForm form,
                        BindingResult binding, Model model, RedirectAttributes flash) {
        if (!binding.hasErrors()) {
            try {
                registros.criar(ator.getName(), form);
                flash.addFlashAttribute("sucesso", "Comunicado publicado.");
                return "redirect:" + BASE;
            } catch (IllegalArgumentException ex) { binding.reject("cadastro", ex.getMessage()); }
        }
        return preparar(ator.getName(), model, form, BASE + "/novo", "Novo comunicado");
    }

    @GetMapping("/{id}/editar")
    public String editar(Authentication ator, @PathVariable Long id, Model model) {
        return preparar(ator.getName(), model, ComunicadoForm.from(registros.buscar(ator.getName(), id)), BASE + "/" + id + "/editar", "Editar comunicado");
    }

    @PostMapping("/{id}/editar")
    public String atualizar(Authentication ator, @PathVariable Long id,
                            @Valid @ModelAttribute("form") ComunicadoForm form, BindingResult binding,
                            Model model, RedirectAttributes flash) {
        if (!binding.hasErrors()) {
            try {
                registros.atualizar(ator.getName(), id, form);
                flash.addFlashAttribute("sucesso", "Comunicado atualizado.");
                return "redirect:" + BASE;
            } catch (IllegalArgumentException ex) { binding.reject("cadastro", ex.getMessage()); }
        }
        return preparar(ator.getName(), model, form, BASE + "/" + id + "/editar", "Editar comunicado");
    }

    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(Authentication ator, @PathVariable Long id, Model model) {
        var item = registros.buscar(ator.getName(), id);
        model.addAttribute("tituloConfirmacao", "Excluir comunicado");
        model.addAttribute("avisoExclusao", "O comunicado deixará de aparecer nos detalhes do evento.");
        model.addAttribute("nomeRegistro", item.getTitulo());
        model.addAttribute("acao", BASE + "/" + id + "/excluir");
        model.addAttribute("voltar", BASE);
        return "excluir";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(Authentication ator, @PathVariable Long id, RedirectAttributes flash) {
        try {
            registros.excluir(ator.getName(), id);
            flash.addFlashAttribute("sucesso", "Comunicado excluído.");
        } catch (IllegalStateException | org.springframework.dao.DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Este registro possui vínculos e não pode ser excluído.");
        }
        return "redirect:" + BASE;
    }

    private String preparar(String email, Model model, ComunicadoForm form, String acao, String titulo) {
        model.addAttribute("form", form);
        model.addAttribute("acao", acao);
        model.addAttribute("titulo", titulo);
        model.addAttribute("eventos", eventos.listarDoCentro(email));
        return "comunicados/form";
    }
}
