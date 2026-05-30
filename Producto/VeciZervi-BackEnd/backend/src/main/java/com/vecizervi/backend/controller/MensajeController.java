package com.vecizervi.backend.controller;

import com.vecizervi.backend.model.Mensaje;
import com.vecizervi.backend.model.Trabajo;
import com.vecizervi.backend.model.Usuario;
import com.vecizervi.backend.repository.MensajeRepository;
import com.vecizervi.backend.repository.TrabajoRepository;
import com.vecizervi.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private TrabajoRepository trabajoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // ── REST: historial de mensajes de un trabajo ─────────────────────────
    @GetMapping("/{idTrabajo}")
    public ResponseEntity<?> getMensajes(@PathVariable Long idTrabajo) {
        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        if (trabajo == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mensajeRepository.findByTrabajoOrderByFechaEnvioAsc(trabajo));
    }

    // ── REST: enviar mensaje (lo guarda Y lo emite por WebSocket) ─────────
    @PostMapping
    public ResponseEntity<?> postMensaje(@RequestBody Map<String, Object> body) {
        Long idTrabajo  = Long.valueOf(body.get("id_trabajo").toString());
        Long idEmisor   = Long.valueOf(body.get("id_emisor").toString());
        String contenido = body.getOrDefault("contenido", "").toString();

        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario emisor  = usuarioRepository.findById(idEmisor).orElse(null);

        if (trabajo  == null) return ResponseEntity.badRequest().body("Trabajo no encontrado.");
        if (emisor   == null) return ResponseEntity.badRequest().body("Emisor no encontrado.");
        if (contenido.isBlank()) return ResponseEntity.badRequest().body("Contenido vacío.");

        Usuario receptor = null;
        if (body.containsKey("id_receptor")) {
            Long idReceptor = Long.valueOf(body.get("id_receptor").toString());
            if (idReceptor > 0) receptor = usuarioRepository.findById(idReceptor).orElse(null);
        }

        Mensaje m = new Mensaje();
        m.setTrabajo(trabajo);
        m.setEmisor(emisor);
        m.setReceptor(receptor);
        m.setContenido(contenido);
        Mensaje guardado = mensajeRepository.save(m);

        // Emitir por WebSocket a todos los suscritos al tópico del trabajo
        messagingTemplate.convertAndSend("/topic/chat/" + idTrabajo, guardado);

        return ResponseEntity.ok(guardado);
    }

    // ── WebSocket: recibe mensaje y lo distribuye en tiempo real ──────────
    @MessageMapping("/chat/{idTrabajo}")
    public void recibirMensajeWS(
            @DestinationVariable Long idTrabajo,
            @Payload Map<String, Object> payload) {

        Long idEmisor   = Long.valueOf(payload.get("id_emisor").toString());
        String contenido = payload.getOrDefault("contenido", "").toString();

        Trabajo trabajo = trabajoRepository.findById(idTrabajo).orElse(null);
        Usuario emisor  = usuarioRepository.findById(idEmisor).orElse(null);

        if (trabajo == null || emisor == null || contenido.isBlank()) return;

        Mensaje m = new Mensaje();
        m.setTrabajo(trabajo);
        m.setEmisor(emisor);
        m.setContenido(contenido);
        Mensaje guardado = mensajeRepository.save(m);

        // Envía a todos los suscritos a este tópico
        messagingTemplate.convertAndSend("/topic/chat/" + idTrabajo, guardado);
    }
}