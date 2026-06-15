package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Categoria;
import com.vecizervi.backend.model.EstadoTrabajo;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.CategoriaRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import com.vecizervi.util.SanitizadorXSS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trabajos")
public class TrabajoController {

    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    @PostMapping("/publicar")
    public ResponseEntity<?> publicarTrabajo(@RequestBody Trabajo nuevoTrabajo,
                                             @RequestParam Long idCliente,
                                             @RequestParam Long idCategoria) {
        // S05 XSS: sanitizar campos de texto antes de validar y guardar
        nuevoTrabajo.setTitulo(SanitizadorXSS.limpiar(nuevoTrabajo.getTitulo()));
        nuevoTrabajo.setDescripcion(SanitizadorXSS.limpiar(nuevoTrabajo.getDescripcion()));
        nuevoTrabajo.setComuna(SanitizadorXSS.limpiar(nuevoTrabajo.getComuna()));

        if (nuevoTrabajo.getPrecio() == null || nuevoTrabajo.getPrecio() <= 0)
            return ResponseEntity.badRequest().body("El precio debe ser un número mayor a 0.");
        if (nuevoTrabajo.getTitulo() == null || nuevoTrabajo.getTitulo().length() < 5)
            return ResponseEntity.badRequest().body("El título debe ser más descriptivo.");

        Usuario cliente     = usuarioRepository.findById(idCliente).orElse(null);
        Categoria categoria = categoriaRepository.findById(idCategoria).orElse(null);

        if (cliente   == null) return ResponseEntity.badRequest().body("Cliente no encontrado.");
        if (categoria == null) return ResponseEntity.badRequest().body("Categoría no encontrada.");

        nuevoTrabajo.setCliente(cliente);
        nuevoTrabajo.setCategoria(categoria);
        nuevoTrabajo.setEstado(EstadoTrabajo.Disponible);

        return ResponseEntity.ok(trabajoRepository.save(nuevoTrabajo));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Trabajo>> listarTrabajosDisponibles() {
        return ResponseEntity.ok(trabajoRepository.findByEstado(EstadoTrabajo.Disponible));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<?> getTrabajosPorCliente(@PathVariable Long idCliente) {
        Usuario cliente = usuarioRepository.findById(idCliente).orElse(null);
        if (cliente == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(trabajoRepository.findByCliente(cliente));
    }

    @GetMapping
    public List<Trabajo> getTodosLosTrabajos() {
        return trabajoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trabajo> getTrabajoPorId(@PathVariable Long id) {
        return trabajoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/editar")
    public ResponseEntity<?> editarTrabajo(@PathVariable Long id, @RequestBody Trabajo datosNuevos) {
        return trabajoRepository.findById(id).map(t -> {
            // S05 XSS: sanitizar antes de actualizar
            t.setTitulo(SanitizadorXSS.limpiar(datosNuevos.getTitulo()));
            t.setDescripcion(SanitizadorXSS.limpiar(datosNuevos.getDescripcion()));
            t.setPrecio(datosNuevos.getPrecio());
            t.setComuna(SanitizadorXSS.limpiar(datosNuevos.getComuna()));
            if (datosNuevos.getEstado() != null) t.setEstado(datosNuevos.getEstado());
            return ResponseEntity.ok(trabajoRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> marcarComoAsignado(@PathVariable Long id) {
        return trabajoRepository.findById(id).map(t -> {
            t.setEstado(EstadoTrabajo.Asignado);
            return ResponseEntity.ok(trabajoRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> marcarComoFinalizado(@PathVariable Long id) {
        return trabajoRepository.findById(id).map(t -> {
            t.setEstado(EstadoTrabajo.Finalizado);
            t.setFechaFinalizacion(LocalDateTime.now());
            return ResponseEntity.ok(trabajoRepository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTrabajo(@PathVariable Long id) {
        if (!trabajoRepository.existsById(id))
            return ResponseEntity.notFound().build();
        trabajoRepository.deleteById(id);
        return ResponseEntity.ok("Trabajo eliminado correctamente.");
    }
}