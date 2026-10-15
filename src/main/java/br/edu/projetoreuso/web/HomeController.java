package br.edu.projetoreuso.web;

import br.edu.projetoreuso.evento.EventoService;
import br.edu.projetoreuso.usuario.UsuarioService;
import br.edu.projetoreuso.web.form.CadastroAlunoForm;
import br.edu.projetoreuso.web.form.UsuarioForm;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import usuario.CentroAcademico;
import usuario.Universidade;
import usuario.Usuario;

@Controller
public class HomeController {
    private final EventoService eventos;
    private final UsuarioService usuarios;

    public HomeController(EventoService eventos, UsuarioService usuarios) {
        this.eventos = eventos;
        this.usuarios = usuarios;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("eventos", eventos.listarPublicos(null).stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("cadastro", new CadastroAlunoForm());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cadastro") CadastroAlunoForm form,
                            BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) return "cadastro";
        try {
            usuarios.cadastrarAluno(form);
            flash.addFlashAttribute("sucesso", "Conta criada. Você já pode entrar.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            binding.reject("cadastro", ex.getMessage());
            return "cadastro";
        }
    }

    @GetMapping("/painel")
    public String painel(Authentication authentication, Model model) {
        Usuario usuario = usuarios.buscarAtual(authentication.getName());
        model.addAttribute("usuario", usuario);
        model.addAttribute("eventosRecentes", eventos.listarPublicos(null).stream().limit(4).toList());
        if (usuario instanceof CentroAcademico) {
            return "redirect:/centro-academico/eventos";
        }
        if (usuario instanceof Universidade) {
            model.addAttribute("usuariosGerenciados", usuarios.listarGerenciados(authentication.getName()));
        }
        return "painel";
    }

    @GetMapping("/minha-conta")
    public String minhaConta(Authentication authentication, Model model) {
        model.addAttribute("form", usuarios.formularioDaMinhaConta(authentication.getName()));
        model.addAttribute("titulo", "Minha conta");
        model.addAttribute("acao", "/minha-conta");
        model.addAttribute("propriaConta", true);
        return "usuarios/form";
    }

    @PostMapping("/minha-conta")
    public String atualizarMinhaConta(Authentication authentication,
                                      @Valid @ModelAttribute("form") UsuarioForm form,
                                      BindingResult binding, Model model, RedirectAttributes flash,
                                      HttpServletRequest request, HttpServletResponse response) {
        if (binding.hasErrors()) {
            model.addAttribute("titulo", "Minha conta");
            model.addAttribute("acao", "/minha-conta");
            model.addAttribute("propriaConta", true);
            return "usuarios/form";
        }
        try {
            usuarios.atualizarMinhaConta(authentication.getName(), form);
            Usuario atualizado = usuarios.buscarAtual(form.getEmail());
            var novaAutenticacao = new UsernamePasswordAuthenticationToken(atualizado, null, atualizado.getAuthorities());
            novaAutenticacao.setDetails(authentication.getDetails());
            var contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(novaAutenticacao);
            SecurityContextHolder.setContext(contexto);
            new HttpSessionSecurityContextRepository().saveContext(contexto, request, response);
            flash.addFlashAttribute("sucesso", "Seus dados foram atualizados.");
            return "redirect:/minha-conta";
        } catch (IllegalArgumentException ex) {
            binding.reject("conta", ex.getMessage());
            model.addAttribute("titulo", "Minha conta");
            model.addAttribute("acao", "/minha-conta");
            model.addAttribute("propriaConta", true);
            return "usuarios/form";
        }
    }
    @GetMapping("/erro")
    public String erro(@RequestParam(defaultValue = "404") int status, Model model, HttpServletResponse response) {
        int codigo = status == 403 ? 403 : 404;
        response.setStatus(codigo);
        model.addAttribute("tituloErro", codigo == 403 ? "Acesso não autorizado" : "Página não encontrada");
        return "erro";
    }
}
