package br.edu.projetoreuso.patterns.observer;

/** Somente os campos visíveis da prévia. Senha e token não pertencem ao estado observado. */
public record DadosUsuarioPrevia(String nome, String email, String tipoUsuario, String curso, String areaAtuacao) { }
