package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Resena;
import com.vecizervi.backend.model.Trabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByTrabajo(Trabajo trabajo);
}