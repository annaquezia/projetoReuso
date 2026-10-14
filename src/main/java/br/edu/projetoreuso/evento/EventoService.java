package br.edu.projetoreuso.evento;

import br.edu.projetoreuso.usuario.CentroAcademicoRepository;
import br.edu.projetoreuso.usuario.PalestranteRepository;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import br.edu.projetoreuso.web.form.EventoForm;
import evento.Evento;
import br.edu.projetoreuso.inscricao.InscricaoRepository;
import evento.EventoStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.util.StringUtils;
import usuario.CentroAcademico;
import usuario.Palestrante;
import usuario.Usuario;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventoService {
    private final EventoRepository eventos;
    private final InscricaoRepository inscricoes;
    private final UsuarioRepository usuarios;
    private final CentroAcademicoRepository centros;
    private final PalestranteRepository palestrantes;

    public EventoService(EventoRepository eventos, UsuarioRepository usuarios,
                         CentroAcademicoRepository centros, PalestranteRepository palestrantes, InscricaoRepository inscricoes) {
        this.eventos = eventos;
        this.inscricoes = inscricoes;
        this.usuarios = usuarios;
        this.centros = centros;
        this.palestrantes = palestrantes;
    }

    @Transactional(readOnly = true)
    public List<Evento> listarPublicos(EventoStatus status) {
        return status == null
                ? eventos.findAllByAtivoTrueOrderByDataAscHorarioAsc()
                : eventos.findAllByAtivoTrueAndStatusOrderByDataAscHorarioAsc(status);
    }

    @Transactional(readOnly = true)
    public Evento buscarPublico(Long id) {
        return eventos.findByIdAndAtivoTrue(id).orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Evento> listarDoCentro(String emailAtor) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        return eventos.findAllByOrganizador_IdAndAtivoTrueOrderByDataAscHorarioAsc(centro.getId());
    }

    @Transactional(readOnly = true)
    public List<Palestrante> palestrantesDoCentro(String emailAtor) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        return palestrantes.findAllByCentroAcademico_IdOrderByNome(centro.getId()).stream()
                .filter(Usuario::isAtivo).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Evento criar(String emailAtor, EventoForm form) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        validar(form);
        Evento evento = new Evento();
        preencher(evento, form, centro);
        evento.setEventoState(null);
        return eventos.save(evento);
    }

    @Transactional(readOnly = true)
    public Evento buscarDoCentro(String emailAtor, Long id) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        Evento evento = eventos.findByIdAndAtivoTrue(id).orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        garantirProprietario(evento, centro);
        return evento;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Evento atualizar(String emailAtor, Long id, EventoForm form) {
        Evento evento = buscarBloqueado(emailAtor, id);
        if (evento.getStatus() == EventoStatus.CANCELADO || evento.getStatus() == EventoStatus.FINALIZADO)
            throw new IllegalStateException("Eventos cancelados ou finalizados não podem ser alterados.");
        validar(form);
        if (form.getQuantidadeVagas() < inscricoes.countByEvento_IdAndAtivaTrue(id))
            throw new IllegalArgumentException("As vagas totais não podem ser menores que as inscrições confirmadas.");
        preencher(evento, form, evento.getOrganizador());
        long ocupadas = inscricoes.countByEvento_IdAndAtivaTrue(id);
        if (ocupadas == evento.getQuantidadeVagas() && evento.getStatus() == EventoStatus.DISPONIVEL) evento.lotar();
        else if (ocupadas < evento.getQuantidadeVagas() && evento.getStatus() == EventoStatus.LOTADO)
            evento.setEventoState(new evento.state.DisponivelState());
        return eventos.save(evento);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelar(String emailAtor, Long id) {
        alterarEstado(emailAtor, id, "CANCELADO");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void lotar(String emailAtor, Long id) {
        alterarEstado(emailAtor, id, "LOTADO");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void finalizar(String emailAtor, Long id) {
        alterarEstado(emailAtor, id, "FINALIZADO");
    }

    private void alterarEstado(String emailAtor, Long id, String acao) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        Evento evento = buscarBloqueado(emailAtor, id);
        switch (acao) {
            case "LOTADO" -> evento.lotar();
            case "CANCELADO" -> evento.cancelar();
            case "FINALIZADO" -> evento.finalizar();
            default -> throw new IllegalArgumentException("Ação de estado desconhecida.");
        }
        eventos.save(evento);

    }

    private void preencher(Evento evento, EventoForm form, CentroAcademico centro) {
        evento.setNome(form.getNome().trim());
        evento.setData(form.getData());
        evento.setHorario(form.getHorario());
        evento.setLocal(form.getLocal().trim());
        evento.setDescricao(form.getDescricao().trim());
        evento.setQuantidadeVagas(form.getQuantidadeVagas());
        evento.setImagemUrl(StringUtils.hasText(form.getImagemUrl()) ? form.getImagemUrl().trim() : null);
        evento.setOrganizador(centro);
        if (form.getPalestranteId() == null) {
            evento.setPalestrante(null);
        } else {
            Palestrante palestrante = palestrantes.findById(form.getPalestranteId())
                    .orElseThrow(() -> new IllegalArgumentException("Palestrante não encontrado."));
            if (palestrante.getCentroAcademico() == null || !palestrante.getCentroAcademico().getId().equals(centro.getId()))
                throw new AccessDeniedException("Selecione um palestrante do seu centro acadêmico.");
            if (!palestrante.isAtivo()) throw new IllegalArgumentException("O palestrante selecionado está inativo.");
            evento.setPalestrante(palestrante);
        }
    }

    private void validar(EventoForm form) {
        if (form.getData() != null && form.getData().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("A data do evento não pode estar no passado.");
        if (form.getQuantidadeVagas() < 1) throw new IllegalArgumentException("Informe pelo menos uma vaga.");
    }

    private CentroAcademico centroPorEmail(String email) {
        Usuario ator = usuarios.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!(ator instanceof CentroAcademico)) throw new AccessDeniedException("Somente centros acadêmicos podem gerenciar eventos.");
        return centros.findById(ator.getId()).orElseThrow(() -> new IllegalArgumentException("Centro acadêmico não encontrado."));
    }

    private void garantirProprietario(Evento evento, CentroAcademico centro) {
        if (!evento.getOrganizador().getId().equals(centro.getId()))
            throw new AccessDeniedException("Você não pode alterar eventos de outro centro acadêmico.");
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void arquivar(String emailAtor, Long id) {
        Evento evento = buscarBloqueado(emailAtor, id);
        evento.setAtivo(false);
        eventos.save(evento);

    }

    private Evento buscarBloqueado(String emailAtor, Long id) {
        CentroAcademico centro = centroPorEmail(emailAtor);
        Evento evento = eventos.findBloqueadoById(id).filter(Evento::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        garantirProprietario(evento, centro);
        return evento;
    }
}
