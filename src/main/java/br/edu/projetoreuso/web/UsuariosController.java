package br.edu.projetoreuso.web;

import br.edu.projetoreuso.usuario.UsuarioService;
import br.edu.projetoreuso.web.form.UsuarioForm;
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
@RequestMapping("/usuarios")
public class UsuariosController {
    private final UsuarioService usuarios;

    public UsuariosController(UsuarioService usuarios) { this.usuarios = usuarios; }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        model.addAttribute("usuarios", usuarios.listarGerenciados(authentication.getName()));
        model.addAttribute("titulo", "Usuários da organização");
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Authentication authentication, Model model) {
        var tipos = usuarios.tiposCriaveis(authentication.getName());
        UsuarioForm form = new UsuarioForm();
        form.setTipoUsuario(tipos.get(0));
        prepararFormulario(model, form, "Novo usuário", "/usuarios/novo", false);
        model.addAttribute("tipos", tipos);
        return "usuarios/novo";
    }

    @PostMapping("/novo")
    public String criar(Authentication authentication, @Valid @ModelAttribute("form") UsuarioForm form,
                        BindingResult binding, Model model, RedirectAttributes flash) {
        var tipos = usuarios.tiposCriaveis(authentication.getName());
        if (binding.hasErrors()) {
            prepararFormulario(model, form, "Novo usuário", "/usuarios/novo", false);
            model.addAttribute("tipos", tipos);
            return "usuarios/novo";
        }
        try {
            usuarios.criarGerenciado(authentication.getName(), form);
            flash.addFlashAttribute("sucesso", "Usuário criado.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException ex) {
            binding.reject("usuario", ex.getMessage());
            prepararFormulario(model, form, "Novo usuário", "/usuarios/novo", false);
            model.addAttribute("tipos", tipos);
            return "usuarios/novo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(Authentication authentication, @PathVariable Long id, Model model) {
        UsuarioForm form = usuarios.formularioGerenciado(authentication.getName(), id);
        prepararFormulario(model, form, "Editar usuário", "/usuarios/" + id + "/editar", false);
        model.addAttribute("tipos", java.util.List.of(form.getTipoUsuario()));
        return "usuarios/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(Authentication authentication, @PathVariable Long id,
                            @Valid @ModelAttribute("form") UsuarioForm form, BindingResult binding,
                            Model model, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            prepararFormulario(model, form, "Editar usuário", "/usuarios/" + id + "/editar", false);
            model.addAttribute("tipos", java.util.List.of(form.getTipoUsuario()));
            return "usuarios/editar";
        }
        try {
            usuarios.atualizarGerenciado(authentication.getName(), id, form);
            flash.addFlashAttribute("sucesso", "Usuário atualizado.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException ex) {
            binding.reject("usuario", ex.getMessage());
            prepararFormulario(model, form, "Editar usuário", "/usuarios/" + id + "/editar", false);
            model.addAttribute("tipos", java.util.List.of(form.getTipoUsuario()));
            return "usuarios/editar";
        }
    }

    @PostMapping("/{id}/status")
    public String alternarStatus(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        boolean ativo = usuarios.alternarAtivo(authentication.getName(), id);
        flash.addFlashAttribute("sucesso", ativo ? "Usuário reativado." : "Usuário desativado.");
        return "redirect:/usuarios";
    }

    private void prepararFormulario(Model model, UsuarioForm form, String titulo, String acao, boolean propriaConta) {
        model.addAttribute("form", form);
        model.addAttribute("titulo", titulo);
        model.addAttribute("acao", acao);
        model.addAttribute("propriaConta", propriaConta);
    }

    @GetMapping("/{id}/desativar")
    public String confirmarDesativacao(Authentication authentication, @PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarios.buscarGerenciado(authentication.getName(), id));
        return "usuarios/desativar";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(Authentication authentication, @PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            usuarios.desativar(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Usuário desativado. O histórico foi preservado.");
            return "redirect:/usuarios";
        } catch (IllegalStateException ex) {
            model.addAttribute("usuario", usuarios.buscarGerenciado(authentication.getName(), id));
            model.addAttribute("erro", ex.getMessage());
            return "usuarios/desativar";
        }
    }

    @GetMapping("/{id}/excluir")
    public String confirmarExclusao(Authentication authentication, @PathVariable Long id, Model model) {
        var usuario = usuarios.buscarGerenciado(authentication.getName(), id);
        model.addAttribute("nomeRegistro", usuario.getNome());
        model.addAttribute("tituloConfirmacao", "Excluir usuário");
        model.addAttribute("avisoExclusao", "Contas com vínculos não podem ser excluídas. Use Desativar para preservar o histórico.");
        model.addAttribute("acao", "/usuarios/" + id + "/excluir");
        model.addAttribute("voltar", "/usuarios");
        return "excluir";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(Authentication authentication, @PathVariable Long id, RedirectAttributes flash) {
        try {
            usuarios.excluir(authentication.getName(), id);
            flash.addFlashAttribute("sucesso", "Usuário excluído.");
        } catch (IllegalStateException | org.springframework.dao.DataIntegrityViolationException ex) {
            flash.addFlashAttribute("erro", "Não foi possível excluir: o usuário possui vínculos. Use Desativar.");
        }
        return "redirect:/usuarios";
    }
}
