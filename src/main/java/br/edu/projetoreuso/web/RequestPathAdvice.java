package br.edu.projetoreuso.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class RequestPathAdvice {

    @ModelAttribute("requestPath")
    public String requestPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("secaoAtual")
    public String secaoAtual(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        if (caminho.startsWith("/usuarios")) return "usuarios";
        if (caminho.contains("/comunicados")) return "comunicados";
        if (caminho.contains("/eventos")) return "eventos";
        if (caminho.startsWith("/minhas-inscricoes")) return "inscricoes";
        if (caminho.startsWith("/minha-conta")) return "conta";
        return "inicio";
    }
}
