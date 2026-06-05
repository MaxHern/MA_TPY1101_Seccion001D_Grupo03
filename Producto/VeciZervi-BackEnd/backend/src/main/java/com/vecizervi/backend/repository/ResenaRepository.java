package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Resena;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByTrabajo(Trabajo trabajo);
    List<Resena> findByReceptor(Usuario receptor);
    boolean existsByTrabajoAndEmisor(Trabajo trabajo, Usuario emisor);
}