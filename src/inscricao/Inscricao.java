package inscricao;

import evento.Evento;
import usuario.Aluno;

import java.time.LocalDateTime;

public class Inscricao {
    private Long id;
    private Aluno aluno;
    private Evento evento;
    private LocalDateTime dataInscricao;
    private StatusInscricao status;
}
