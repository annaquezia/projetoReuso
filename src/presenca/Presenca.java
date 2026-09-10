package presenca;

import inscricao.Inscricao;
import usuario.CentroAcademico;

import java.time.LocalDateTime;

public class Presenca {
    private Long id;
    private Inscricao inscricao;
    private LocalDateTime dataValidacao;
    private CentroAcademico validadoPor;
}
