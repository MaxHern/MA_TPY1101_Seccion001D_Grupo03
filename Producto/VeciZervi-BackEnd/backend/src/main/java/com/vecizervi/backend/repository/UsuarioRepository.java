package com.vecizervi.backend.repository;

import com.vecizervi.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // la consulta SQL: SELECT * FROM usuarios WHERE correo = ?
    Usuario findByCorreo(String correo); 
    
}
