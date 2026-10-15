package br.edu.projetoreuso.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import usuario.Palestrante;

import java.util.List;

public interface PalestranteRepository extends JpaRepository<Palestrante, Long> {
    List<Palestrante> findAllByCentroAcademico_IdOrderByNome(Long centroAcademicoId);
    boolean existsByCentroAcademico_Id(Long centroAcademicoId);
}
