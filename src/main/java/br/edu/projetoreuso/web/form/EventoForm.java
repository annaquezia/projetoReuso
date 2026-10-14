package br.edu.projetoreuso.web.form;

import evento.Evento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventoForm {
    @NotBlank
    private String nome;
    @NotNull
    private LocalDate data;
    @NotNull
    private LocalTime horario;
    @NotBlank
    private String local;
    @NotBlank
    private String descricao;
    @Min(1)
    private int quantidadeVagas;
    private String imagemUrl;
    private Long palestranteId;

    public static EventoForm from(Evento evento) {
        EventoForm form = new EventoForm();
        form.nome = evento.getNome();
        form.data = evento.getData();
        form.horario = evento.getHorario();
        form.local = evento.getLocal();
        form.descricao = evento.getDescricao();
        form.quantidadeVagas = evento.getQuantidadeVagas();
        form.imagemUrl = evento.getImagemUrl();
        form.palestranteId = evento.getPalestrante() == null ? null : evento.getPalestrante().getId();
        return form;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getQuantidadeVagas() { return quantidadeVagas; }
    public void setQuantidadeVagas(int quantidadeVagas) { this.quantidadeVagas = quantidadeVagas; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public Long getPalestranteId() { return palestranteId; }
    public void setPalestranteId(Long palestranteId) { this.palestranteId = palestranteId; }
}
