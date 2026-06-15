package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Resena;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.ResenaRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import com.vecizervi.backend.service.ResenaService;
import com.vecizervi.util.SanitizadorXSS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    @Autowired private ResenaService resenaService;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ResenaRepository resenaRepository;

    @GetMapping
    public List<Resena> listarResenas() {
        return resenaService.findAll();
    }

    @GetMapping("/trabajo/{idTrabajo}")
    public ResponseEntity<?> getResenasPorTrabajo(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resenaService.findByTrabajo(trabajo));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getResenasPorReceptor(@PathVariable Long idUsuario) {
        Usuario receptor = usuarioRepository.findById(idUsuario).orElse(null);
        if (receptor == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resenaRepository.findByReceptor(receptor));
    }

    @PostMapping
    public ResponseEntity<?> crearResena(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("id_trabajo") || !body.containsKey("id_emisor") ||
            !body.containsKey("id_receptor") || !body.containsKey("estrellas"))
            return ResponseEntity.badRequest().body("Faltan campos obligatorios.");

        Long idTrabajo  = Long.valueOf(body.get("id_trabajo").toString());
        Long idEmisor   = Long.valueOf(body.get("id_emisor").toString());
        Long idReceptor = Long.valueOf(body.get("id_receptor").toString());
        Integer estrellas = Integer.valueOf(body.get("estrellas").toString());

        // S05 XSS: sanitizar comentario
        String comentario = SanitizadorXSS.limpiarPermitirVacio(
            body.getOrDefault("comentario", "").toString()
        );

        if (estrellas < 1 || estrellas > 5)
            return ResponseEntity.badRequest().body("Las estrellas deben ser entre 1 y 5.");

        Trabajo trabajo  = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario emisor   = usuarioRepository.findById(idEmisor).orElse(null);
        Usuario receptor = usuarioRepository.findById(idReceptor).orElse(null);

        if (trabajo  == null) return ResponseEntity.badRequest().body("Trabajo no encontrado.");
        if (emisor   == null) return ResponseEntity.badRequest().body("Emisor no encontrado.");
        if (receptor == null) return ResponseEntity.badRequest().body("Receptor no encontrado.");

        boolean yaReseno = resenaRepository.existsByTrabajoAndEmisor(trabajo, emisor);
        if (yaReseno)
            return ResponseEntity.badRequest().body("Ya has calificado este trabajo. Puedes ver tu reseña en el perfil del usuario.");

        Resena r = new Resena();
        r.setTrabajo(trabajo);
        r.setEmisor(emisor);
        r.setReceptor(receptor);
        r.setEstrellas(estrellas);
        r.setComentario(comentario);
        Resena guardada = resenaService.save(r);

        List<Resena> todasResenas = resenaRepository.findByReceptor(receptor);
        double promedio = todasResenas.stream()
            .mapToInt(Resena::getEstrellas).average().orElse(0.0);
        receptor.setCalificacionPromedio(Math.round(promedio * 100.0) / 100.0);
        usuarioRepository.save(receptor);

        return ResponseEntity.ok(guardada);
    }
}