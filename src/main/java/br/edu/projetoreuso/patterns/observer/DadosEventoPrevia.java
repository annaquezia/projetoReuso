package br.edu.projetoreuso.patterns.observer;

/** Strings permitem prévias de rascunhos ainda incompletos, sem executar a validação de gravação. */
public record DadosEventoPrevia(String nome, String descricao, String data, String horario,
                               String local, String quantidadeVagas) { }
