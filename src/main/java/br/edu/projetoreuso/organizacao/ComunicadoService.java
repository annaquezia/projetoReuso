package br.edu.projetoreuso.organizacao;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.web.form.ComunicadoForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ComunicadoService {
    private final ComunicadoRepository registros;
    private final AcessoCentroService acesso;
    private final EventoRepository eventos;

    public ComunicadoService(ComunicadoRepository registros, AcessoCentroService acesso, EventoRepository eventos) {
        this.registros = registros;
        this.acesso = acesso;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<Comunicado> listar(String email) {
        return registros.findAllByEvento_Organizador_IdOrderByIdDesc(acesso.centro(email).getId());
    }

    @Transactional(readOnly = true)
    public List<Comunicado> publicados(Long eventoId) {
        return registros.findAllByEvento_IdOrderByIdDesc(eventoId);
    }

    @Transactional(readOnly = true)
    public Comunicado buscar(String email, Long id) {
        var item = registros.findComEventoById(id).orElseThrow(() -> new IllegalArgumentException("Registro não encontrado."));
        acesso.conferir(email, item.getEvento().getOrganizador().getId());
        return item;
    }

    @Transactional
    public Comunicado criar(String email, ComunicadoForm form) {
        acesso.centro(email);
        var item = new Comunicado();
        preencher(email, item, form);
        return registros.save(item);
    }

    @Transactional
    public Comunicado atualizar(String email, Long id, ComunicadoForm form) {
        var item = buscar(email, id);
        preencher(email, item, form);
        return registros.save(item);
    }

    @Transactional
    public void excluir(String email, Long id) {
        var item = buscar(email, id);
        registros.delete(item);
        registros.flush();
    }

    private void preencher(String email, Comunicado item, ComunicadoForm form) {
        var evento = eventos.findByIdAndAtivoTrue(form.getEventoId())
                .orElseThrow(() -> new IllegalArgumentException("Selecione um evento ativo."));
        acesso.conferir(email, evento.getOrganizador().getId());
        item.setEvento(evento);
        item.setTitulo(form.getTitulo().trim());
        item.setMensagem(form.getMensagem().trim());
    }
}
