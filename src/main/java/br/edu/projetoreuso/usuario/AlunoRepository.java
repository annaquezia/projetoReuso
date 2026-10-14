package br.edu.projetoreuso.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import usuario.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByMatriculaIgnoreCase(String matricula);
    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);
}
