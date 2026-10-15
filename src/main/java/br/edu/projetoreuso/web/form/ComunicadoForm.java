package br.edu.projetoreuso.web.form;

import br.edu.projetoreuso.organizacao.Comunicado;
import jakarta.validation.constraints.*;

public class ComunicadoForm {
    @NotBlank @Size(max = 160)
    private String titulo;
    @NotBlank @Size(max = 2000)
    private String mensagem;
    @NotNull
    private Long eventoId;

    public static ComunicadoForm from(Comunicado item) {
        ComunicadoForm form = new ComunicadoForm();
        form.titulo = item.getTitulo();
        form.mensagem = item.getMensagem();
        form.eventoId = item.getEvento().getId();
        return form;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
}
