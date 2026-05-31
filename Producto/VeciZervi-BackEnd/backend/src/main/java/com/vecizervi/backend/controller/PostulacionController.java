package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Postulacion;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.PostulacionRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionController {

    @Autowired private PostulacionRepository postulacionRepository;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // POST postular a un trabajo
    @PostMapping
    public ResponseEntity<?> postular(@RequestBody Map<String, Object> body) {
        Long idTrabajo    = Long.valueOf(body.get("id_trabajo").toString());
        Long idTrabajador = Long.valueOf(body.get("id_trabajador").toString());
        String mensaje    = body.getOrDefault("mensaje_presentacion", "").toString();

        Trabajo  trabajo    = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario  trabajador = usuarioRepository.findById(idTrabajador).orElse(null);

        if (trabajo    == null) return ResponseEntity.badRequest().body("Trabajo no encontrado.");
        if (trabajador == null) return ResponseEntity.badRequest().body("Trabajador no encontrado.");

        Postulacion p = new Postulacion();
        p.setTrabajo(trabajo);
        p.setTrabajador(trabajador);
        p.setMensajePresentacion(mensaje);

        return ResponseEntity.ok(postulacionRepository.save(p));
    }

    // GET postulaciones de un trabajo
    @GetMapping("/trabajo/{idTrabajo}")
    public ResponseEntity<?> getPostulaciones(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(postulacionRepository.findByTrabajo(trabajo));
    }

    // GET trabajos donde el usuario postulo (para el inbox)
    @GetMapping("/trabajador/{idTrabajador}")
    public ResponseEntity<?> getTrabajosPorTrabajador(@PathVariable Long idTrabajador) {
        Usuario trabajador = usuarioRepository.findById(idTrabajador).orElse(null);
        if (trabajador == null) return ResponseEntity.notFound().build();

        List<Trabajo> trabajos = postulacionRepository.findByTrabajador(trabajador)
                .stream()
                .map(Postulacion::getTrabajo)
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(trabajos);
    }

    // GET todas (admin)
    @GetMapping
    public List<Postulacion> getAll() {
        return postulacionRepository.findAll();
    }
}