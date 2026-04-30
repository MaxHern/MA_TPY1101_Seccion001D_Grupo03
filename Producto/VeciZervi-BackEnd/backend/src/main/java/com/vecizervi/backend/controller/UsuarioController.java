package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController 
@RequestMapping("/api/usuarios") 
public class UsuarioController {

    @Autowired 
    private UsuarioRepository usuarioRepository;

    // Scrum 1 y 2: Registro de Usuario y Contraseña
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        
        if (nuevoUsuario.getRut() == null || nuevoUsuario.getCorreo() == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios (RUT o Correo).");
        }
        if (nuevoUsuario.getContrasenaEnCriptada() == null || nuevoUsuario.getContrasenaEnCriptada().length() < 8) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres.");
        }
        // Guardar el usuario real en la base de datos de MySQL
        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok("Usuario registrado y validado con éxito.");
    }

    // Scrum 3: Iniciar Sesión
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario loginRequest) {
        
        // Buscamos al usuario en la BD usando su correo
        Usuario usuarioDB = usuarioRepository.findByCorreo(loginRequest.getCorreo());
        if (usuarioDB == null || !usuarioDB.getContrasenaEnCriptada().equals(loginRequest.getContrasenaEnCriptada())) {
            return ResponseEntity.status(401).body("Correo o contraseña incorrectos.");
        }
        return ResponseEntity.ok("jwt_token_simulado_12345");
    }
}
