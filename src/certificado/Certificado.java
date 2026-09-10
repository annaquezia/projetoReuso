package certificado;

import evento.Evento;
import usuario.Aluno;

import java.time.LocalDateTime;

public class Certificado {
    private Long id;
    private Aluno aluno;
    private Evento evento;
    private LocalDateTime dataEmissao;
    private String assinaturaReitoria;
    private String caminhoArquivo;
}
