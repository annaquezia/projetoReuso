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
}
