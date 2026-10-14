package br.edu.projetoreuso.web;

import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrosController {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        model.addAttribute("requestPath", request.getRequestURI());
        model.addAttribute("tituloErro", "Registro não encontrado");
        model.addAttribute("mensagem", ex.getMessage());
        return "erro";
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String proibido(AccessDeniedException ex, Model model, HttpServletRequest request) {
        model.addAttribute("requestPath", request.getRequestURI());
        model.addAttribute("tituloErro", "Acesso não autorizado");
        model.addAttribute("mensagem", "Você não tem permissão para acessar este registro.");
        return "erro";
    }
}
