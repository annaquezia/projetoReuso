package br.edu.projetoreuso.web;

import br.edu.projetoreuso.evento.EventoService;
import br.edu.projetoreuso.web.form.EventoForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/centro-academico/eventos")
public class EventoGestaoController {
    private final EventoService eventos;

    public EventoGestaoController(EventoService eventos) {
        this.eventos = eventos;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        model.addAttribute("eventos", eventos.listarDoCentro(authentication.getName()));
        return "eventos/gestao-lista";
    }

    @GetMapping("/novo")
    public String novo(Authentication authentication, Model model) {
        preparar(authentication, model, new EventoForm(), "Criar evento", "/centro-academico/eventos/novo");
        return "eventos/novo";
    }

    @PostMapping("/novo")
    public String criar(Authentication authentication, @Valid @ModelAttribute("form") EventoForm form,
                        BindingResult binding, Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            preparar(authentication, model, form, "Criar evento", "/centro-academico/eventos/novo");
            return "eventos/novo";
        }
        try {
            var evento = eventos.criar(authentication.getName(), form);
            flash.addFlashAttribute("sucesso", "Evento criado.");
            return "redirect:/centro-academico/eventos/" + evento.getId() + "/editar";
        } catch (IllegalArgumentException ex) {
            binding.reject("evento", ex.getMessage());
            preparar(authentication, model, form, "Criar evento", "/centro-academico/eventos/novo");
            return "eventos/novo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(Authentication authentication, @PathVariable Long id, Model model) {
        preparar(authentication, model, EventoForm.from(eventos.buscarDoCentro(authentication.getName(), id)),
                "Editar evento", "/centro-academico/eventos/" + id + "/editar");
        model.addAttribute("eventoId", id);
        return "eventos/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(Authentication authentication, @PathVariable Long id,
                            @Valid @ModelAttribute("form") EventoForm form, BindingResult binding,
                            Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            preparar(authentication, model, form, "Editar evento", "/centro-academico/eventos/" + id + "/editar");
            model.addAttribute("eventoId", id);
            return "eventos/editar";
        }
        try {
            eventos.atualizar(authentication.getName(), id, form);
            flash.addFlashAttribute("sucesso", "Evento atualizado.");
            return "redirect:/centro-academico/eventos";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            binding.reject("evento", ex.getMessage());
            preparar(authentication, model, form, "Editar evento", "/centro-academico/eventos/" + id + "/editar");
            model.addAttribute("eventoId", id);
            return "eventos/editar";
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.cancelar(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Evento cancelado.");
        } catch (IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/centro-academico/eventos";
    }

    @PostMapping("/{id}/lotar")
    public String lotar(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.lotar(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Evento marcado como lotado.");
        } catch (IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/centro-academico/eventos";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.finalizar(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Evento finalizado.");
        } catch (IllegalStateException ex) {
            flash.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/centro-academico/eventos";
    }

    private void preparar(Authentication authentication, Model model, EventoForm form, String titulo, String acao) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("palestrantes", eventos.palestrantesDoCentro(authentication.getName()));
    }

    @GetMapping("/{id}/arquivar")
    public String confirmarArquivamento(Authentication authentication, @PathVariable Long id, Model model) {
        model.addAttribute("evento", eventos.buscarDoCentro(authentication.getName(), id));
        return "eventos/arquivar";
    }

    @PostMapping("/{id}/arquivar")
    public String arquivar(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        eventos.arquivar(authentication.getName(), id);
        flash.addFlashAttribute("sucesso", "Evento arquivado. O histórico foi preservado.");
        return "redirect:/centro-academico/eventos";
    }

    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(Authentication authentication, @PathVariable Long id, Model model) {
        var evento = eventos.buscarDoCentro(authentication.getName(), id);
        model.addAttribute("nomeRegistro", evento.getNome());
        model.addAttribute("tituloConfirmacao", "Excluir evento");
        model.addAttribute("avisoExclusao", "Eventos com inscrições ou comunicados não podem ser excluídos. Use Arquivar para preservar o histórico.");
        model.addAttribute("acao", "/centro-academico/eventos/" + id + "/excluir");
        model.addAttribute("voltar", "/centro-academico/eventos");
        return "excluir";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        try {
            eventos.excluir(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Evento excluído.");
        } catch (IllegalStateException | org.springframework.dao.DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não foi possível excluir: o evento possui vínculos. Use Arquivar.");
        }
        return "redirect:/centro-academico/eventos";
    }
}
