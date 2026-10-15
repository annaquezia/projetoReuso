package br.edu.projetoreuso.organizacao;

import br.edu.projetoreuso.usuario.CentroAcademicoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usuario.CentroAcademico;

@Service
public class AcessoCentroService {
    private final CentroAcademicoRepository centros;

    public AcessoCentroService(CentroAcademicoRepository centros) { this.centros = centros; }

    @Transactional(readOnly = true)
    public CentroAcademico centro(String email) {
        return centros.findByEmailIgnoreCaseAndAtivoTrue(email)
                .orElseThrow(() -> new AccessDeniedException("Entre com uma conta ativa de centro acadêmico."));
    }

    public void conferir(String email, Long centroId) {
        if (!centro(email).getId().equals(centroId))
            throw new AccessDeniedException("Este registro pertence a outro centro acadêmico.");
    }
}
