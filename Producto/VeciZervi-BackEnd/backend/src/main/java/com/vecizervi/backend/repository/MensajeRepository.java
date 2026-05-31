package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Query directa por id — evita el bug de findByTrabajo con objeto
    @Query("SELECT m FROM Mensaje m WHERE m.trabajo.id = :idTrabajo ORDER BY m.fechaEnvio ASC")
    List<Mensaje> findByIdTrabajo(@Param("idTrabajo") Long idTrabajo);

    // Para el inbox — trabajos donde el usuario participó
    @Query("SELECT DISTINCT m.trabajo FROM Mensaje m WHERE m.emisor = :usuario OR m.receptor = :usuario")
    List<Trabajo> findTrabajosConMensajesByUsuario(@Param("usuario") Usuario usuario);
}