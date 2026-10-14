package br.edu.projetoreuso.evento;

import evento.Evento;
import evento.EventoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    @EntityGraph(attributePaths = {"organizador", "palestrante"})
    List<Evento> findAllByAtivoTrueOrderByDataAscHorarioAsc();
    @EntityGraph(attributePaths = {"organizador", "palestrante"})
    List<Evento> findAllByAtivoTrueAndStatusOrderByDataAscHorarioAsc(EventoStatus status);
    @EntityGraph(attributePaths = {"organizador", "palestrante"})
    List<Evento> findAllByOrganizador_IdAndAtivoTrueOrderByDataAscHorarioAsc(Long organizadorId);
    @EntityGraph(attributePaths = {"organizador", "palestrante"})
    Optional<Evento> findByIdAndAtivoTrue(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> findBloqueadoById(@Param("id") Long id);
}
