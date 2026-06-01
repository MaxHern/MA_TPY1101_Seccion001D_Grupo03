package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Historial privado: mensajes entre dos usuarios en un trabajo
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.trabajo.id = :idTrabajo
        AND (
            (m.emisor.id = :idUsuario1 AND (m.receptor.id = :idUsuario2 OR m.receptor IS NULL))
            OR
            (m.emisor.id = :idUsuario2 AND (m.receptor.id = :idUsuario1 OR m.receptor IS NULL))
        )
        ORDER BY m.fechaEnvio ASC
        """)
    List<Mensaje> findConversacionPrivada(
        @Param("idTrabajo")  Long idTrabajo,
        @Param("idUsuario1") Long idUsuario1,
        @Param("idUsuario2") Long idUsuario2
    );

    // Para el inbox
    @Query("SELECT DISTINCT m.trabajo FROM Mensaje m WHERE m.emisor = :usuario OR m.receptor = :usuario")
    List<Trabajo> findTrabajosConMensajesByUsuario(@Param("usuario") Usuario usuario);
}