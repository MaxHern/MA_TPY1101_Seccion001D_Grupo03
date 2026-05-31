package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Postulacion;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    List<Postulacion> findByTrabajo(Trabajo trabajo);
    List<Postulacion> findByTrabajador(Usuario trabajador);
}
