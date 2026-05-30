package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.MensajeRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // GET historial de mensajes de un trabajo
    @GetMapping("/{idTrabajo}")
    public ResponseEntity<?> getMensajes(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        List<Mensaje> mensajes = mensajeRepository.findByTrabajoOrderByFechaEnvioAsc(trabajo);
        return ResponseEntity.ok(mensajes);
    }

    // POST enviar mensaje
    @PostMapping
    public ResponseEntity<?> postMensaje(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("id_trabajo") || !body.containsKey("id_emisor") || !body.containsKey("contenido"))
                return ResponseEntity.badRequest().body("Faltan campos: id_trabajo, id_emisor, contenido.");

            Long idTrabajo   = Long.valueOf(body.get("id_trabajo").toString());
            Long idEmisor    = Long.valueOf(body.get("id_emisor").toString());
            String contenido = body.get("contenido").toString().trim();

            if (contenido.isEmpty())
                return ResponseEntity.badRequest().body("El contenido no puede estar vacÃ­o.");

            Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
            Usuario emisor  = usuarioRepository.findById(idEmisor).orElse(null);

            if (trabajo == null) return ResponseEntity.badRequest().body("Trabajo no encontrado: " + idTrabajo);
            if (emisor  == null) return ResponseEntity.badRequest().body("Emisor no encontrado: " + idEmisor);

            Usuario receptor = null;
            if (body.containsKey("id_receptor")) {
                try {
                    Long idReceptor = Long.valueOf(body.get("id_receptor").toString());
                    if (idReceptor > 0) receptor = usuarioRepository.findById(idReceptor).orElse(null);
                } catch (Exception ignored) {}
            }

            Mensaje m = new Mensaje();
            m.setTrabajo(trabajo);
            m.setEmisor(emisor);
            m.setReceptor(receptor);
            m.setContenido(contenido);

            Mensaje guardado = mensajeRepository.save(m);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}