package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired private UsuarioRepository usuarioRepository;

    // BCrypt: se crea una sola vez y se reutiliza
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    // ── Registro: hashea la contraseña con BCrypt antes de guardar ────────
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        if (nuevoUsuario.getRut() == null || nuevoUsuario.getCorreo() == null)
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
        if (nuevoUsuario.getContrasenaEnCriptada() == null ||
            nuevoUsuario.getContrasenaEnCriptada().length() < 8)
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres.");

        // BCRYPT: hashear la contraseña antes de guardarla
        String hash = bcrypt.encode(nuevoUsuario.getContrasenaEnCriptada());
        nuevoUsuario.setContrasenaEnCriptada(hash);

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    // ── Login: bloqueo a los 3 intentos + verificación BCrypt ─────────────
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario loginRequest) {
        Usuario usuarioDB = usuarioRepository.findByCorreo(loginRequest.getCorreo());
        if (usuarioDB == null)
            return ResponseEntity.status(401).body("Correo incorrecto.");

        // Verificar bloqueo
        if (usuarioDB.getCuentaBloqueadaHasta() != null) {
            if (LocalDateTime.now().isBefore(usuarioDB.getCuentaBloqueadaHasta())) {
                long minutosRestantes = java.time.Duration.between(
                    LocalDateTime.now(), usuarioDB.getCuentaBloqueadaHasta()
                ).toMinutes() + 1;
                return ResponseEntity.status(403).body(
                    "Cuenta bloqueada. Podrás intentarlo en " + minutosRestantes + " minuto(s).\n" +
                    "Puedes recuperar tu contraseña usando '¿Olvidaste tu contraseña?'"
                );
            } else {
                // Desbloquear automáticamente si ya pasó el tiempo
                usuarioDB.setIntentosFallidos(0);
                usuarioDB.setCuentaBloqueadaHasta(null);
                usuarioRepository.save(usuarioDB);
            }
        }

        // Verificar contraseña con BCrypt
        boolean claveCorrecta = bcrypt.matches(
            loginRequest.getContrasenaEnCriptada(),
            usuarioDB.getContrasenaEnCriptada()
        );

        if (!claveCorrecta) {
            usuarioDB.setIntentosFallidos(usuarioDB.getIntentosFallidos() + 1);
            int intentos = usuarioDB.getIntentosFallidos();

            // BLOQUEO A LOS 3 INTENTOS
            if (intentos >= 3) {
                usuarioDB.setCuentaBloqueadaHasta(LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(usuarioDB);
                return ResponseEntity.status(403).body(
                    "Cuenta bloqueada por 3 intentos fallidos.\n" +
                    "Puedes recuperar tu contraseña usando '¿Olvidaste tu contraseña?'"
                );
            }

            usuarioRepository.save(usuarioDB);
            int restantes = 3 - intentos;
            return ResponseEntity.status(401).body(
                "Contraseña incorrecta. Te quedan " + restantes + " intento(s)."
            );
        }

        // Login exitoso
        usuarioDB.setIntentosFallidos(0);
        usuarioDB.setCuentaBloqueadaHasta(null);
        usuarioRepository.save(usuarioDB);
        return ResponseEntity.ok(usuarioDB);
    }

    @GetMapping
    public List<Usuario> getTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id,
                                               @RequestBody Usuario datosNuevos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombres(datosNuevos.getNombres());
            usuario.setApellidos(datosNuevos.getApellidos());
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(usuario);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cambiar-clave")
    public ResponseEntity<?> cambiarClave(@PathVariable Long id,
                                          @RequestParam String claveAntigua,
                                          @RequestParam String claveNueva) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (!bcrypt.matches(claveAntigua, usuario.getContrasenaEnCriptada()))
                return ResponseEntity.badRequest().body("La clave antigua no coincide");
            if (claveNueva.length() < 8)
                return ResponseEntity.badRequest().body("Mínimo 8 caracteres");
            usuario.setContrasenaEnCriptada(bcrypt.encode(claveNueva));
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Contraseña cambiada con éxito");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado.");
    }

    // ── Recuperar contraseña: PASO 1 ──────────────────────────────────────
    @PostMapping("/recuperar-clave")
    public ResponseEntity<?> recuperarClave(@RequestParam String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null)
            return ResponseEntity.status(404).body("No existe una cuenta con ese correo.");

        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        usuario.setTokenRecuperacion(codigo);
        // Al recuperar contraseña, desbloquear la cuenta también
        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueadaHasta(null);
        usuarioRepository.save(usuario);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Código generado correctamente");
        respuesta.put("codigo", codigo);
        return ResponseEntity.ok(respuesta);
    }

    // ── PASO 2: verificar token ───────────────────────────────────────────
    @PostMapping("/verificar-token")
    public ResponseEntity<?> verificarToken(@RequestParam String correo,
                                            @RequestParam String token) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null || usuario.getTokenRecuperacion() == null)
            return ResponseEntity.status(400).body("Token inválido.");
        if (!usuario.getTokenRecuperacion().equals(token))
            return ResponseEntity.status(400).body("Código incorrecto.");
        return ResponseEntity.ok("Token válido.");
    }

    // ── PASO 3: nueva contraseña (también hasheada con BCrypt) ────────────
    @PostMapping("/nueva-clave")
    public ResponseEntity<?> nuevaClave(@RequestParam String correo,
                                        @RequestParam String token,
                                        @RequestParam String nuevaClave) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        if (usuario == null || usuario.getTokenRecuperacion() == null)
            return ResponseEntity.status(400).body("Token inválido.");
        if (!usuario.getTokenRecuperacion().equals(token))
            return ResponseEntity.status(400).body("Código incorrecto.");
        if (nuevaClave.length() < 8)
            return ResponseEntity.badRequest().body("Mínimo 8 caracteres.");

        // BCRYPT: hashear la nueva contraseña
        usuario.setContrasenaEnCriptada(bcrypt.encode(nuevaClave));
        usuario.setTokenRecuperacion(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}