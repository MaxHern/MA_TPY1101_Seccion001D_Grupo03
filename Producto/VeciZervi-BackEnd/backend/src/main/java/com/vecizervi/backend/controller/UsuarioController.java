package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.UsuarioRepository;
import com.vecizervi.backend.security.JwtUtil;
import com.vecizervi.util.SanitizadorXSS;

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
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario nuevoUsuario) {
        if (nuevoUsuario.getRut() == null || nuevoUsuario.getCorreo() == null)
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
        if (nuevoUsuario.getContrasenaEnCriptada() == null ||
            nuevoUsuario.getContrasenaEnCriptada().length() < 8)
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres.");

        nuevoUsuario.setNombres(SanitizadorXSS.limpiar(nuevoUsuario.getNombres()));
        nuevoUsuario.setApellidos(SanitizadorXSS.limpiar(nuevoUsuario.getApellidos()));

        String hash = bcrypt.encode(nuevoUsuario.getContrasenaEnCriptada());
        nuevoUsuario.setContrasenaEnCriptada(hash);

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Usuario loginRequest) {
        Usuario usuarioDB = usuarioRepository.findByCorreo(loginRequest.getCorreo());
        if (usuarioDB == null)
            return ResponseEntity.status(401).body("Correo o contraseña incorrectos.");

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
                usuarioDB.setIntentosFallidos(0);
                usuarioDB.setCuentaBloqueadaHasta(null);
                usuarioRepository.save(usuarioDB);
            }
        }

        boolean claveCorrecta = bcrypt.matches(
            loginRequest.getContrasenaEnCriptada(),
            usuarioDB.getContrasenaEnCriptada()
        );

        if (!claveCorrecta) {
            usuarioDB.setIntentosFallidos(usuarioDB.getIntentosFallidos() + 1);
            int intentos = usuarioDB.getIntentosFallidos();
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
                "Correo o contraseña incorrectos. Te quedan " + restantes + " intento(s)."
            );
        }

        usuarioDB.setIntentosFallidos(0);
        usuarioDB.setCuentaBloqueadaHasta(null);
        usuarioRepository.save(usuarioDB);

        String token = jwtUtil.generarToken(usuarioDB.getId(), usuarioDB.getRol());

        // S03/S05 FIX: DTO manual — incluye rut/correo/fechaNacimiento porque el
        // frontend SÍ los necesita para mostrar el perfil del propio usuario logueado.
        // Lo que NUNCA viaja es la contraseña, el token de recuperación ni los
        // contadores de bloqueo — esos no tienen ninguna razón para salir del backend.
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("idUsuario", usuarioDB.getId());
        datosUsuario.put("rut", usuarioDB.getRut());
        datosUsuario.put("nombres", usuarioDB.getNombres());
        datosUsuario.put("apellidos", usuarioDB.getApellidos());
        datosUsuario.put("fechaNacimiento", usuarioDB.getFechaNacimiento());
        datosUsuario.put("correo", usuarioDB.getCorreo());
        datosUsuario.put("rol", usuarioDB.getRol());
        datosUsuario.put("calificacionPromedio", usuarioDB.getCalificacionPromedio());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("usuario", datosUsuario);
        respuesta.put("token", token);
        return ResponseEntity.ok(respuesta);
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
            usuario.setNombres(SanitizadorXSS.limpiar(datosNuevos.getNombres()));
            usuario.setApellidos(SanitizadorXSS.limpiar(datosNuevos.getApellidos()));
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

    // NOTA IMPORTANTE PARA LA DEFENSA / DEMO EN CLASE:
    // En producción real, el código de recuperación se enviaría por correo electrónico
    // y NUNCA viajaría en la respuesta HTTP (eso era la vulnerabilidad S03 corregida).
    // Como este proyecto académico no tiene servicio de envío de correo configurado,
    // se mantiene el código en la respuesta SOLO para que el flujo sea demostrable
    // en la demo, dejando documentado que es un riesgo aceptado temporalmente.
    @PostMapping("/recuperar-clave")
    public ResponseEntity<?> recuperarClave(@RequestParam String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Si el correo existe en nuestro sistema, recibirás el código");

        if (usuario == null) {
            return ResponseEntity.ok(respuesta);
        }

        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        usuario.setTokenRecuperacion(codigo);
        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueadaHasta(null);
        usuarioRepository.save(usuario);

        // Riesgo aceptado temporalmente (documentado en informe): sin servicio de
        // correo real, se devuelve el código para que la demo sea funcional.
        respuesta.put("codigo", codigo);

        return ResponseEntity.ok(respuesta);
    }

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
        usuario.setContrasenaEnCriptada(bcrypt.encode(nuevaClave));
        usuario.setTokenRecuperacion(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
}