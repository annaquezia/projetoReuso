package evento;

import localizacao.Localizacao;
import usuario.CentroAcademico;
import usuario.Palestrante;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evento {

    private Long id;
    private String nome;
    private LocalDate data;
    private LocalTime horario;
    private String local;
    private String descricao;
    private int quantidadeVagas;
    private Palestrante palestrante;
    private CentroAcademico organizador;
    private Localizacao localizacao;
    private StatusEvento status;
}
