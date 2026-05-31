package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Resena;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import com.vecizervi.backend.service.ResenaService;
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

    // GET todas las reseñas (admin)
    @GetMapping
    public List<Resena> listarResenas() {
        return resenaService.findAll();
    }

    // FIX: GET reseñas por trabajo — el Android lo pide con 404 antes
    @GetMapping("/trabajo/{idTrabajo}")
    public ResponseEntity<?> getResenasPorTrabajo(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resenaService.findByTrabajo(trabajo));
    }

    // FIX: POST recibe campos planos (id_trabajo, id_emisor, id_receptor)
    // en vez de objetos anidados que causaban error 500
    @PostMapping
    public ResponseEntity<?> crearResena(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("id_trabajo") || !body.containsKey("id_emisor") ||
            !body.containsKey("id_receptor") || !body.containsKey("estrellas"))
            return ResponseEntity.badRequest().body("Faltan campos obligatorios.");

        Long idTrabajo  = Long.valueOf(body.get("id_trabajo").toString());
        Long idEmisor   = Long.valueOf(body.get("id_emisor").toString());
        Long idReceptor = Long.valueOf(body.get("id_receptor").toString());
        Integer estrellas = Integer.valueOf(body.get("estrellas").toString());
        String comentario = body.getOrDefault("comentario", "").toString();

        if (estrellas < 1 || estrellas > 5)
            return ResponseEntity.badRequest().body("Las estrellas deben ser entre 1 y 5.");

        Trabajo trabajo  = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario emisor   = usuarioRepository.findById(idEmisor).orElse(null);
        Usuario receptor = usuarioRepository.findById(idReceptor).orElse(null);

        if (trabajo  == null) return ResponseEntity.badRequest().body("Trabajo no encontrado.");
        if (emisor   == null) return ResponseEntity.badRequest().body("Emisor no encontrado.");
        if (receptor == null) return ResponseEntity.badRequest().body("Receptor no encontrado.");

        Resena r = new Resena();
        r.setTrabajo(trabajo);
        r.setEmisor(emisor);
        r.setReceptor(receptor);
        r.setEstrellas(estrellas);
        r.setComentario(comentario);

        return ResponseEntity.ok(resenaService.save(r));
    }
}
