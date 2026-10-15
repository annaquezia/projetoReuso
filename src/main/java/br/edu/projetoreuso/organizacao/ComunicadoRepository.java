package br.edu.projetoreuso.organizacao;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComunicadoRepository extends JpaRepository<Comunicado, Long> {
    boolean existsByEvento_Id(Long eventoId);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"evento"})
    List<Comunicado> findAllByEvento_Organizador_IdOrderByIdDesc(Long centroId);
    List<Comunicado> findAllByEvento_IdOrderByIdDesc(Long eventoId);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"evento"})
    java.util.Optional<Comunicado> findComEventoById(Long id);
}
