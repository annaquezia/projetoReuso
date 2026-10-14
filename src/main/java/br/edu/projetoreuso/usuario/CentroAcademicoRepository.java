package br.edu.projetoreuso.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import usuario.CentroAcademico;

import java.util.List;

public interface CentroAcademicoRepository extends JpaRepository<CentroAcademico, Long> {
    List<CentroAcademico> findAllByUniversidade_IdOrderByNome(Long universidadeId);
}
