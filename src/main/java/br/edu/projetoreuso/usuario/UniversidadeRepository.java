package br.edu.projetoreuso.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import usuario.Universidade;

public interface UniversidadeRepository extends JpaRepository<Universidade, Long> {
}
