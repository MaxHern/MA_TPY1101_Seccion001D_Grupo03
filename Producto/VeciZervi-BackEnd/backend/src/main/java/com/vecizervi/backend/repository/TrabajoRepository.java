package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Trabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {
    
    // Spring Boot creará el SQL: SELECT * FROM trabajos WHERE estado = ?
    List<Trabajo> findByEstado(String estado);
    
}
