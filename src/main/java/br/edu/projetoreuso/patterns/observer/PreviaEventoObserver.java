package br.edu.projetoreuso.patterns.observer;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Observer 2: atualiza a apresentação de título, descrição, agenda, local e capacidade do evento. */
public final class PreviaEventoObserver implements FormularioObserver<DadosEventoPrevia> {
    private Map<String, String> previa = Map.of();

    @Override public void atualizar(DadosEventoPrevia dados) {
        previa = Map.of("nome", padrao(dados.nome(), "Nome do evento"),
                "descricao", padrao(dados.descricao(), "A descrição aparecerá aqui."),
                "quando", data(dados.data()) + " · " + horario(dados.horario()),
                "local", padrao(dados.local(), "Local a definir"), "vagas", vagas(dados.quantidadeVagas()));
    }
    public Map<String, String> getPrevia() { return previa; }
    private String texto(String valor) { return valor == null ? "" : valor.trim(); }
    private String padrao(String valor, String padrao) { return texto(valor).isEmpty() ? padrao : texto(valor); }
    private String data(String valor) {
        try { return LocalDate.parse(texto(valor)).format(DateTimeFormatter.ofPattern("dd/MM/uuuu")); }
        catch (DateTimeException ex) { return "Data a definir"; }
    }
    private String horario(String valor) {
        try { return LocalTime.parse(texto(valor)).format(DateTimeFormatter.ofPattern("HH:mm")); }
        catch (DateTimeException ex) { return "Horário a definir"; }
    }
    private String vagas(String valor) {
        try {
            int total = Integer.parseInt(texto(valor));
            return total > 0 ? total + (total == 1 ? " vaga" : " vagas") : "Informe as vagas";
        } catch (NumberFormatException ex) { return "Informe as vagas"; }
    }
}
