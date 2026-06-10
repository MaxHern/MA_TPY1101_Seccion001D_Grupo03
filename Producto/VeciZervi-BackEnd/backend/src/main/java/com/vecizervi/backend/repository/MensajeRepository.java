package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

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
        @Param("idTrabajo") Long idTrabajo,
        @Param("idUsuario1") Long idUsuario1,
        @Param("idUsuario2") Long idUsuario2
    );

    // Prueba 25: ORDER BY fecha del último mensaje → conversación más reciente arriba
    @Query("""
        SELECT m.trabajo FROM Mensaje m
        WHERE m.emisor = :usuario OR m.receptor = :usuario
        GROUP BY m.trabajo
        ORDER BY MAX(m.fechaEnvio) DESC
    """)
    List<Trabajo> findTrabajosConMensajesByUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT DISTINCT m.emisor FROM Mensaje m WHERE m.trabajo.id = :idTrabajo AND m.emisor.id != :idDueno")
    List<Usuario> findParticipantesByTrabajo(
        @Param("idTrabajo") Long idTrabajo,
        @Param("idDueno") Long idDueno
    );

    // FIX-29/30: contar mensajes no leídos por conversación (trabajo + receptor)
    long countByTrabajo_IdAndReceptor_IdAndLeidoFalse(Long idTrabajo, Long idReceptor);

    // FIX-29/30: mensajes no leídos que recibió un usuario en un trabajo concreto
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.trabajo.id = :idTrabajo
        AND m.receptor.id = :idReceptor
        AND m.leido = false
    """)
    List<Mensaje> findNoLeidosByTrabajoAndReceptor(
        @Param("idTrabajo") Long idTrabajo,
        @Param("idReceptor") Long idReceptor
    );
}