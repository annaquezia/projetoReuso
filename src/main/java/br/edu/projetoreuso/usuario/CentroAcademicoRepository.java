package br.edu.projetoreuso.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import usuario.CentroAcademico;

import java.util.List;
import java.util.Optional;

public interface CentroAcademicoRepository extends JpaRepository<CentroAcademico, Long> {
    List<CentroAcademico> findAllByUniversidade_IdOrderByNome(Long universidadeId);
    Optional<CentroAcademico> findByEmailIgnoreCaseAndAtivoTrue(String email);
}
