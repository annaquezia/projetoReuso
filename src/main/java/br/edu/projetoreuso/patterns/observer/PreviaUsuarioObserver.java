package br.edu.projetoreuso.patterns.observer;

import java.util.Map;

/** Observer 1: transforma alterações do rascunho na prévia de usuário exibida pela tela. */
public final class PreviaUsuarioObserver implements FormularioObserver<DadosUsuarioPrevia> {
    private static final Map<String, String> PERFIS = Map.of(
            "ALUNO", "Estudante", "CENTRO_ACADEMICO", "Centro acadêmico",
            "PALESTRANTE", "Palestrante", "UNIVERSIDADE", "Universidade");
    private Map<String, String> previa = Map.of();

    @Override public void atualizar(DadosUsuarioPrevia dados) {
        String detalhe = texto(dados.curso());
        if (detalhe.isEmpty()) detalhe = texto(dados.areaAtuacao());
        previa = Map.of("nome", padrao(dados.nome(), "Nome do usuário"),
                "perfil", PERFIS.getOrDefault(texto(dados.tipoUsuario()), "Perfil"),
                "email", padrao(dados.email(), "E-mail ainda não informado"), "detalhe", detalhe);
    }
    public Map<String, String> getPrevia() { return previa; }
    private String texto(String valor) { return valor == null ? "" : valor.trim(); }
    private String padrao(String valor, String padrao) { return texto(valor).isEmpty() ? padrao : texto(valor); }
}
