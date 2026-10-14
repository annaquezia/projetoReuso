package br.edu.projetoreuso.inscricao;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<InscricaoEvento, Long> {
    Optional<InscricaoEvento> findByAluno_IdAndEvento_Id(Long alunoId, Long eventoId);
    long countByEvento_IdAndAtivaTrue(Long eventoId);
    @EntityGraph(attributePaths = {"evento", "evento.organizador"})
    List<InscricaoEvento> findAllByAluno_IdOrderByDataInscricaoDesc(Long alunoId);
    @Query("select i.evento.id from InscricaoEvento i where i.id = :id and i.aluno.id = :aluno")
    Optional<Long> eventoDaInscricao(@Param("id") Long id, @Param("aluno") Long aluno);
}
