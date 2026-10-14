package br.edu.projetoreuso.web;

import br.edu.projetoreuso.patterns.observer.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Pontes HTTP para as prévias das telas. Não acessa repositórios nem grava dados. */
@RestController
public class PreviasController {
    @PostMapping({"/usuarios/previa", "/minha-conta/previa"})
    public Map<String, String> usuario(@RequestBody DadosUsuarioPrevia dados) {
        FormularioSubject<DadosUsuarioPrevia> subject = new FormularioSubject<>();
        PreviaUsuarioObserver observer = new PreviaUsuarioObserver();
        subject.registrarObserver(observer);
        subject.alterar(dados);
        return observer.getPrevia();
    }

    @PostMapping("/centro-academico/eventos/previa")
    public Map<String, String> evento(@RequestBody DadosEventoPrevia dados) {
        FormularioSubject<DadosEventoPrevia> subject = new FormularioSubject<>();
        PreviaEventoObserver observer = new PreviaEventoObserver();
        subject.registrarObserver(observer);
        subject.alterar(dados);
        return observer.getPrevia();
    }
}
