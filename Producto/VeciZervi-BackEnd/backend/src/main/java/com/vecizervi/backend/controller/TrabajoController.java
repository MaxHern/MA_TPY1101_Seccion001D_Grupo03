package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.repository.TrabajoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trabajos")
public class TrabajoController {

    @Autowired
    private TrabajoRepository trabajoRepository;

    // Scrum 4: Publicar un trabajo
    @PostMapping("/publicar")
    public ResponseEntity<?> publicarTrabajo(@RequestBody Trabajo nuevoTrabajo) {
        
        if (nuevoTrabajo.getPrecio() == null || nuevoTrabajo.getPrecio() <= 0) {
            return ResponseEntity.badRequest().body("El precio debe ser un número mayor a 0.");
        }
        if (nuevoTrabajo.getTitulo() == null || nuevoTrabajo.getTitulo().length() < 5) {
            return ResponseEntity.badRequest().body("El título debe ser más descriptivo.");
        }

        trabajoRepository.save(nuevoTrabajo);
        return ResponseEntity.ok("¡Tu trabajo ha sido publicado exitosamente!");
    }

    // Scrum 5: Ver lista de trabajos disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<Trabajo>> listarTrabajosDisponibles() {
        List<Trabajo> disponibles = trabajoRepository.findByEstado("DISPONIBLE");
        return ResponseEntity.ok(disponibles);
    }

    // Scrum 6: Marcar trabajo como "Asignado"
    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> marcarComoAsignado(@PathVariable Long id) {
        Optional<Trabajo> trabajoOpt = trabajoRepository.findById(id);
        
        if (trabajoOpt.isPresent()) {
            Trabajo trabajo = trabajoOpt.get();
            trabajo.setEstado("ASIGNADO");
            trabajoRepository.save(trabajo);
            return ResponseEntity.ok("El trabajo ha sido marcado como Asignado. Ya no aparecerá en la lista.");
        }
        return ResponseEntity.notFound().build();
    }

    // Scrum 7: Marcar trabajo como "Finalizado"
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> marcarComoFinalizado(@PathVariable Long id) {
        Optional<Trabajo> trabajoOpt = trabajoRepository.findById(id);
        
        if (trabajoOpt.isPresent()) {
            Trabajo trabajo = trabajoOpt.get();
            trabajo.setEstado("FINALIZADO");
            trabajo.setFechaFinalizacion(LocalDateTime.now());
            trabajoRepository.save(trabajo);
            return ResponseEntity.ok("Trabajo finalizado con éxito a las " + trabajo.getFechaFinalizacion());
        }
        return ResponseEntity.notFound().build();
    }
}