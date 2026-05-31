package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByTrabajoOrderByFechaEnvioAsc(Trabajo trabajo);

    // Trae todos los trabajos donde el usuario participó como emisor o receptor
    @Query("SELECT DISTINCT m.trabajo FROM Mensaje m WHERE m.emisor = :usuario OR m.receptor = :usuario")
    List<Trabajo> findTrabajosConMensajesByUsuario(@Param("usuario") Usuario usuario);

    // Último mensaje de un trabajo (para mostrar preview en el inbox)
    @Query("SELECT m FROM Mensaje m WHERE m.trabajo = :trabajo ORDER BY m.fechaEnvio DESC LIMIT 1")
    Mensaje findUltimoMensajeByTrabajo(@Param("trabajo") Trabajo trabajo);
}