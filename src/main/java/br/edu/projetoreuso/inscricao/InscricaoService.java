package br.edu.projetoreuso.inscricao;

import br.edu.projetoreuso.evento.EventoRepository;
import br.edu.projetoreuso.usuario.UsuarioRepository;
import br.edu.projetoreuso.config.VarianteProduto;
import evento.Evento;
import evento.EventoStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import usuario.Aluno;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscricaoService {
    private final InscricaoRepository inscricoes;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final VarianteProduto variante;

    public InscricaoService(InscricaoRepository inscricoes, EventoRepository eventos,
                           UsuarioRepository usuarios, VarianteProduto variante) {
        this.inscricoes = inscricoes;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.variante = variante;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InscricaoEvento inscrever(String email, Long eventoId) {
        variante.exigirInscricoes();
        Aluno aluno = aluno(email);
        Evento evento = eventos.findBloqueadoById(eventoId).filter(Evento::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        InscricaoEvento inscricao = inscricoes.findByAluno_IdAndEvento_Id(aluno.getId(), eventoId).orElse(null);
        if (inscricao != null && inscricao.isAtiva()) throw new IllegalStateException("Você já está inscrito neste evento.");
        if (evento.getStatus() != EventoStatus.DISPONIVEL || !futuro(evento))
            throw new IllegalStateException("Este evento não está disponível para inscrições.");
        long ocupadas = inscricoes.countByEvento_IdAndAtivaTrue(eventoId);
        if (ocupadas >= evento.getQuantidadeVagas()) throw new IllegalStateException("As vagas deste evento estão esgotadas.");
        if (inscricao == null) {
            inscricao = new InscricaoEvento();
            inscricao.setAluno(aluno);
            inscricao.setEvento(evento);
            inscricao.setDataInscricao(LocalDateTime.now());
        } else {
            inscricao.reativar();
        }
        InscricaoEvento salva = inscricoes.save(inscricao);
        if (ocupadas + 1 == evento.getQuantidadeVagas()) { evento.lotar(); eventos.save(evento); }
        return salva;
    }

    @Transactional(readOnly = true)
    public List<InscricaoEvento> minhas(String email) {
        variante.exigirInscricoes();
        return inscricoes.findAllByAluno_IdOrderByDataInscricaoDesc(aluno(email).getId());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelar(String email, Long id) {
        variante.exigirInscricoes();
        Aluno aluno = aluno(email);
        Long eventoId = inscricoes.eventoDaInscricao(id, aluno.getId())
                .orElseThrow(() -> new AccessDeniedException("Você não pode cancelar esta inscrição."));
        Evento evento = eventos.findBloqueadoById(eventoId).orElseThrow(() -> new IllegalArgumentException("Evento não encontrado."));
        InscricaoEvento inscricao = inscricoes.findById(id).orElseThrow();
        inscricao.cancelar();
        inscricoes.save(inscricao);
        if (evento.isAtivo() && futuro(evento) && evento.getStatus() == EventoStatus.LOTADO) {
            evento.reabrir();
            eventos.save(evento);
        }
    }

    public record Situacao(boolean inscrito, long vagasDisponiveis, boolean podeInscrever) { }
    @Transactional(readOnly = true)
    public Situacao situacao(Evento evento, String email) {
        long vagas = Math.max(0L, evento.getQuantidadeVagas() - inscricoes.countByEvento_IdAndAtivaTrue(evento.getId()));
        boolean inscrito = email != null && usuarios.findByEmailIgnoreCase(email)
                .filter(Aluno.class::isInstance)
                .flatMap(usuario -> inscricoes.findByAluno_IdAndEvento_Id(usuario.getId(), evento.getId()))
                .map(InscricaoEvento::isAtiva).orElse(false);
        return new Situacao(inscrito, vagas, variante.isInscricoesHabilitadas() && evento.isAtivo()
                && futuro(evento) && evento.getStatus() == EventoStatus.DISPONIVEL && vagas > 0);
    }
    private Aluno aluno(String email) {
        var usuario = usuarios.findByEmailIgnoreCase(email).orElseThrow(() -> new AccessDeniedException("Entre com sua conta de estudante."));
        if (!(usuario instanceof Aluno aluno) || !aluno.isAtivo()) throw new AccessDeniedException("Somente estudantes ativos podem se inscrever.");
        return aluno;
    }
    private boolean futuro(Evento evento) { return LocalDateTime.of(evento.getData(), evento.getHorario()).isAfter(LocalDateTime.now()); }
}
